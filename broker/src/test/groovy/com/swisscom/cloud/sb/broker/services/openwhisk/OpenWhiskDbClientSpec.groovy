package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class OpenWhiskDbClientSpec extends Specification{
    private final String SUBJECT = "testing"

    OpenWhiskDbClient openWhiskDbClient
    RestTemplateFactory restTemplateFactory
    MockRestServiceServer mockServer
    ObjectMapper mapper

    def setup() {
        restTemplateFactory = Mock(RestTemplateFactory)
        RestTemplate restTemplate = new RestTemplate()
        restTemplateFactory.buildWithBasicAuthentication(_,_) >> restTemplate
        mockServer = MockRestServiceServer.createServer(restTemplate)
        mapper = new ObjectMapper()
        and:
        openWhiskDbClient = new OpenWhiskDbClient(new OpenWhiskConfig(openWhiskDbProtocol: "http",
                openWhiskDbHost: "openwhiskHost", openWhiskDbPort: "1234", openWhiskDbLocalUser: "ubuntu",
                openWhiskDbHostname: "localhost"), restTemplateFactory)
//            println("openWhiskDbClient = ${openWhiskDbClient.getVars()}")
    }

    def "Retrieve subject - positive"() {
        def response = """{
                                "_id":"testing",
                                "_rev":"1-fc4d69eb866e5032b08669aec49c0ea1",
                                "subject":"testing",
                                "namespaces":[
                                    {
                                        "name":"testing",
                                        "uuid":"62b59324-50f4-456d-b1b8-e9d026bf9cf3",
                                        "key":"FTyhTgIF2GWAyyDWMEaMXyUWpEfKuayMPnSI1XgSH3aV1h9SZIsA4WBlVuZDbMLu"
                                    }
                                ]
                           }"""
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.TEXT_PLAIN))

        when:
        String res = openWhiskDbClient.getSubjectFromDB(SUBJECT)

        then:
        res.getClass() == String
        noExceptionThrown()
    }

    def "Retrieve subject - negative"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST))

        when:
        String res = openWhiskDbClient.getSubjectFromDB(SUBJECT)

        then:
        res == null
        noExceptionThrown()
    }

    def "Insert subject - positive"(){
        given:
        def response = """{
                                "_id":"testing",
                                "_rev":"1-fc4d69eb866e5032b08669aec49c0ea1",
                                "subject":"testing",
                                "namespaces":[
                                    {
                                        "name":"testing",
                                        "uuid":"62b59324-50f4-456d-b1b8-e9d026bf9cf3",
                                        "key":"FTyhTgIF2GWAyyDWMEaMXyUWpEfKuayMPnSI1XgSH3aV1h9SZIsA4WBlVuZDbMLu"
                                    }
                                ]
                           }"""
        JsonNode payload = mapper.readTree(response)
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.TEXT_PLAIN))

        when:
        openWhiskDbClient.insertIntoDatabase(payload)

        then:
        noExceptionThrown()
    }

    def "Insert subject - negative"(){
        given:
        def response = """{
                                "_id":"testing",
                                "_rev":"1-fc4d69eb866e5032b08669aec49c0ea1",
                                "subject":"testing",
                                "namespaces":[
                                    {
                                        "name":"testing",
                                        "uuid":"62b59324-50f4-456d-b1b8-e9d026bf9cf3",
                                        "key":"FTyhTgIF2GWAyyDWMEaMXyUWpEfKuayMPnSI1XgSH3aV1h9SZIsA4WBlVuZDbMLu"
                                    }
                                ]
                           }"""
        JsonNode payload = mapper.readTree(response)
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST))

        when:
        openWhiskDbClient.insertIntoDatabase(payload)

        then:
        def exception = thrown(HttpClientErrorException)
        exception.statusCode == HttpStatus.BAD_REQUEST
    }

}
