package com.swisscom.cloud.sb.broker.services.kubernetes.client.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilderFactory
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Log4j
class KubernetesClient<RESPONSE> {

    KubernetesConfig kubernetesConfig
    RestTemplateBuilderFactory restTemplateBuilderFactory

    @Autowired
    KubernetesClient(KubernetesConfig kubernetesConfig, RestTemplateBuilderFactory restTemplateBuilderFactory) {
        this.kubernetesConfig = kubernetesConfig
        this.restTemplateBuilderFactory = restTemplateBuilderFactory
    }

    ResponseEntity<RESPONSE> exchange(String url, HttpMethod method,
                                      String body, Class<RESPONSE> responseType) {
        def restTemplateBuilder = restTemplateBuilderFactory.build()
        def restTemplate = restTemplateBuilder.withMutualTLS(kubernetesConfig.kubernetesClientCertificate, kubernetesConfig.kubernetesClientKey).build()
        log.info(url + " - " + convertYamlToJson(body))
        return restTemplate.exchange(
                "https://" + kubernetesConfig.getKubernetesHost() + ":" + kubernetesConfig.getKubernetesPort() + "/" +
                        url, method, new HttpEntity<String>(convertYamlToJson(body), getJsonHeaders()), responseType)
    }

    private HttpHeaders getJsonHeaders() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        return headers
    }

    String convertYamlToJson(String yaml) {
        if (yaml == null || yaml.isEmpty()) {
            return ""
        }
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory())
        Object obj = yamlReader.readValue(yaml, Object.class)
        ObjectMapper jsonWriter = new ObjectMapper()
        return jsonWriter.writeValueAsString(obj)
    }

}
