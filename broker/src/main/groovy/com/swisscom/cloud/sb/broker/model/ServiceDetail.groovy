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

package com.swisscom.cloud.sb.broker.model

import com.swisscom.cloud.sb.broker.util.servicedetail.AbstractServiceDetailKey

import javax.persistence.Column
import javax.persistence.Entity

@Entity
class ServiceDetail extends BaseModel{

    @Column(name = '_key')
    String key
    @Column(name = '_value')
    String value
    @Column(name = '_type')
    String type
    @Column(columnDefinition='tinyint(1) default 0')
    boolean uniqueKey

    static ServiceDetail from(String key, String value) { return new ServiceDetail(key: key, value: value) }

    static ServiceDetail from(AbstractServiceDetailKey detailKey, String value) {
        return new ServiceDetail(key: detailKey.key, value: value, type: detailKey.detailType().type)
    }

    @Override
    boolean equals(Object obj) {
        ServiceDetail otherServiceDetail = obj as ServiceDetail
        if (otherServiceDetail == null)
            return false

        return isSameServiceDetail(otherServiceDetail)
    }

    private boolean isSameServiceDetail(ServiceDetail serviceDetail) {
        return (serviceDetail.id > 0 && serviceDetail.id == this.id) ||
                (this.uniqueKey && serviceDetail.key == this.key)
    }
}
