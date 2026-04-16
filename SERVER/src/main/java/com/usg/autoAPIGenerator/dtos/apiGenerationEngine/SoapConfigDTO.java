// SoapConfigDTO.java
package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoapConfigDTO {
    private String version;           // "1.1" or "1.2"
    private String bindingStyle;      // "document" or "rpc"
    private String encodingStyle;     // "literal" or "encoded"
    private String soapAction;
    private String wsdlUrl;
    private String namespace;
    private String serviceName;
    private String portName;
    private Boolean useAsyncPattern;
    private Boolean includeMtom;
    private List<String> soapHeaderElements;
}