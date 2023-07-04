# yts-users

YTS User management API

### What is this service?
- yts-users is responsible to handle all of these operations on users:
    * creating
    * updating
    * deleting

### pods/third parties involved:
- maintenance
- client-users-kyc pod
- client-users (client-gateway pod)

### What does it do?
* Creating new users
* Deleting user data once it's called by maintenance

## Diagrams
- [Architecture](https://git.yolt.io/backend/users/-/blob/master/architecture.puml)