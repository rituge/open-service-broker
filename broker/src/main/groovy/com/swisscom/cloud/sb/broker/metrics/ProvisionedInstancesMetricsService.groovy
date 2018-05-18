package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

@Service
@CompileStatic
class ProvisionedInstancesMetricsService extends ServiceBrokerMetrics {

    private final String PROVISIONED_INSTANCES = "provisionedInstances"

    @Autowired
    ProvisionedInstancesMetricsService(ServiceInstanceRepository serviceInstanceRepository, LastOperationRepository lastOperationRepository) {
        super(serviceInstanceRepository, lastOperationRepository)
    }

    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        // service instance should only be considered if it hasn't been deleted, yet
        return !serviceInstance.deleted
    }

    @Override
    String tag() {
        return ProvisionedInstancesMetricsService.class.getSimpleName()
    }

    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()
        List<ServiceInstance> serviceInstanceList = serviceInstanceRepository.findAll()

        def totalMetrics = retrieveTotalMetrics(serviceInstanceList)
        metrics.add(new Metric<Long>("${PROVISIONED_INSTANCES}.${TOTAL}.${TOTAL}", totalMetrics.total))
        metrics.add(new Metric<Long>("${PROVISIONED_INSTANCES}.${TOTAL}.${SUCCESS}", totalMetrics.totalSuccess))
        metrics.add(new Metric<Long>("${PROVISIONED_INSTANCES}.${TOTAL}.${FAIL}", totalMetrics.totalFailures))
        metrics.add(new Metric<Double>("${PROVISIONED_INSTANCES}.${SUCCESS}.${RATIO}", calculateRatio(totalMetrics.total, totalMetrics.totalSuccess)))
        metrics.add(new Metric<Double>("${PROVISIONED_INSTANCES}.${FAIL}.${RATIO}", calculateRatio(totalMetrics.total, totalMetrics.totalFailures)))

        def totalMetricsPerService = retrieveTotalMetricsPerService(serviceInstanceList)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerService.total, totalMetricsPerService.total, metrics, PROVISIONED_INSTANCES, SERVICE, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerService.total, totalMetricsPerService.totalSuccess, metrics, PROVISIONED_INSTANCES, SERVICE, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerService.total, totalMetricsPerService.totalFailures, metrics, PROVISIONED_INSTANCES, SERVICE, FAIL)

        def totalMetricsPerPlan = retrieveTotalMetricsPerPlan(serviceInstanceList)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerPlan.total, totalMetricsPerPlan.total, metrics, PROVISIONED_INSTANCES, PLAN, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerPlan.total, totalMetricsPerPlan.totalSuccess, metrics, PROVISIONED_INSTANCES, PLAN, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerPlan.total, totalMetricsPerPlan.totalFailures, metrics, PROVISIONED_INSTANCES, PLAN, FAIL)

        return metrics
    }
}