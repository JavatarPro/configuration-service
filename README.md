## Сonfiguration Service ##


### What is this microservice for? ###
Central place to manage application properties for all other µServices.
Automatically update µService with new configuration after pushing of configuration file changes to the repository.

### How does it work? ###

Once any property changes have been pushed to the git repository, Webhook request is sent to the Configuration service. Configuration service parses Webhook payload and retrieves µService name for which you have changed the properties. Then it gets the µService application from Eureka and sends a REFRESH request to all µService instances.



### Basic Configuration ###


```
#!yml

spring:
cloud:
  config:
    server:
      git:
        uri: [git_URI]
        username: [git_user_name]
        password: [git_user_password]
eureka:
instance:
  preferIpAddress: false
  hostname: localhost
client:
  serviceUrl:
    defaultZone: ${vcap.helper.eureka-service.credentials.uri:http://localhost:8761}/eureka/

server:
port: 8888
```

### Deployment instructions ###
1. Create git repository
2. Create Webhook in this git repository on push action