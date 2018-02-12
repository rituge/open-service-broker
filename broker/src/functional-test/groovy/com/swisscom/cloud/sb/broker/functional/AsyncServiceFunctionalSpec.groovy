package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.context.ContextPersistenceService
import com.swisscom.cloud.sb.broker.model.repository.ContextRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.cloud.servicebroker.model.KubernetesContext

class AsyncServiceFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private ContextRepository contextRepository

    private int processDelayInSeconds = DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummy', ServiceProviderLookup.findInternalName(DummyServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance without context"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true, ['delay': String.valueOf(processDelayInSeconds)])

        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
        assertCloudFoundryContext(serviceInstanceGuid)
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }

    def "provision async service instance with CloudFoundryContext"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def context = new CloudFoundryContext("org_id", "space_id")

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, null, context)

        then:
        assertCloudFoundryContext(serviceInstanceGuid)
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "provision async service instance with KubernetesContext"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def context = new KubernetesContext("namespace_guid")

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, null, context)

        then:
        assertKubernetesContext(serviceInstanceGuid)
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    void assertCloudFoundryContext(String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance != null

        assert contextRepository.findByKeyAndServiceInstance(ContextPersistenceService.PLATFORM, serviceInstance) != null
        assert contextRepository.findByKeyAndServiceInstance(ContextPersistenceService.CF_ORGANIZATION_GUID, serviceInstance) != null
        assert contextRepository.findByKeyAndServiceInstance(ContextPersistenceService.CF_SPACE_GUID, serviceInstance) != null
    }

    void assertKubernetesContext(String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance != null

        assert contextRepository.findByKeyAndServiceInstance(ContextPersistenceService.PLATFORM, serviceInstance) != null
        assert contextRepository.findByKeyAndServiceInstance(ContextPersistenceService.KUBENETES_NAMESPACE, serviceInstance) != null
    }
}