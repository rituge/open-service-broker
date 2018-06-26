/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.util.test

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException

class ErrorCodeHelper {
    public static boolean assertServiceBrokerException(ServiceBrokerException ex, ErrorCode errorCode) {
        ex.code == errorCode.code && ex.error_code == errorCode.errorCode
    }

    public static boolean assertServiceBrokerException(ErrorCode errorCode, ServiceBrokerException e) {
        return e.code == errorCode.code && e.error_code == errorCode.errorCode && e.description == errorCode.description && e.httpStatus == errorCode.httpStatus
    }
}
