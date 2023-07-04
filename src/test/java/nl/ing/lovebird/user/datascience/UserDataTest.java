package nl.ing.lovebird.user.datascience;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.github.fge.jackson.NodeType;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.format.AbstractFormatAttribute;
import com.github.fge.jsonschema.format.FormatAttribute;
import com.github.fge.jsonschema.library.DraftV4Library;
import com.github.fge.jsonschema.library.Library;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.processors.data.FullData;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.core.io.ClassPathResource;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for data science topic, if any of these test fail, the topic needs to be modified and changes discussed with the datascience team
 */
class UserDataTest {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .setDateFormat(new StdDateFormat().withColonInTimeZone(false))
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule())
            .registerModule(new JsonComponentModule());
    private final static PodamFactory PODAM_FACTORY = new PodamFactoryImpl();
    private final static JsonSchema SCHEMA = getSchema();

    @Test
    void validateSchema() throws Exception {
        OffloadUserMessage<UserData> message = PODAM_FACTORY.manufacturePojo(UserDataMessage.class);
        JsonNode kycMessageJson = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(message));
        ProcessingReport report = SCHEMA.validate(kycMessageJson);
        assertThat(report.isSuccess()).withFailMessage(report.toString()).isTrue();
    }

    @SneakyThrows
    private static JsonSchema getSchema() {
        ClassPathResource attachmentFile = new ClassPathResource("datascienceJsonSchema.json");
        byte[] fileBytes = attachmentFile.getInputStream().readAllBytes();
        JsonNode schemaJsonNode = OBJECT_MAPPER.readTree(fileBytes);

        Library library = DraftV4Library.get().thaw()
                .addFormatAttribute("date-time", DateTimeAttribute.getInstance())
                .freeze();

        final ValidationConfiguration cfg = ValidationConfiguration.newBuilder()
                .setDefaultLibrary("http://yolt.eu/datascience#", library)
                .freeze();

        final JsonSchemaFactory schemaFactory = JsonSchemaFactory.newBuilder()
                .setValidationConfiguration(cfg)
                .freeze();
        return schemaFactory.getJsonSchema(schemaJsonNode);
    }

    static class DateTimeAttribute extends AbstractFormatAttribute {

        private static final List<String> FORMATS = ImmutableList.of(
                "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}", "yyyy-MM-dd'T'HH:mm:ss((+|-)HH:mm|Z)", "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}((+|-)HH:mm|Z)"
        );

        private static final DateTimeFormatter FORMATTER;

        static {
            final DateTimeParser secFracsParser = new DateTimeFormatterBuilder()
                    .appendLiteral('.').appendFractionOfSecond(1, 12)
                    .toParser();

            final DateTimeParser timeZoneParser = new DateTimeFormatterBuilder()
                    .appendTimeZoneOffset("Z", true, 2, 2)
                    .toParser();

            DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .appendOptional(secFracsParser)
                    .appendOptional(timeZoneParser);

            FORMATTER = builder.toFormatter();
        }

        private static final FormatAttribute INSTANCE = new DateTimeAttribute();

        public static FormatAttribute getInstance() {
            return INSTANCE;
        }

        private DateTimeAttribute() {
            super("date-time", NodeType.STRING);
        }

        @Override
        public void validate(final ProcessingReport report,
                             final MessageBundle bundle, final FullData data)
                throws ProcessingException {
            final String value = data.getInstance().getNode().textValue();

            try {
                FORMATTER.parseDateTime(value);
            } catch (IllegalArgumentException ignored) {
                report.error(newMsg(data, bundle, "err.format.invalidDate")
                        .putArgument("value", value).putArgument("expected", FORMATS));
            }

        }
    }

    static class UserDataMessage extends OffloadUserMessage<UserData> {

        public UserDataMessage(UUID cliendId, UUID userId, int schema_version, UserData payload) {
            super(cliendId, userId, schema_version, payload);
        }
    }
}
