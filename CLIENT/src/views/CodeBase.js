import React, { useState, useEffect, useCallback, useRef } from 'react';
import { 
  ChevronRight, 
  ChevronDown,
  Search,
  Plus,
  Download,
  Copy,
  Settings,
  Code,
  Star,
  X,
  Check,
  AlertCircle,
  Clock,
  Activity,
  Folder,
  FolderOpen,
  Loader,
  FileCode,
  RefreshCw,
  Coffee,
  Box,
  Package,
  Terminal,
  Lock,
  Server,
  Cpu,
  Monitor,
  HardDrive,
  ShieldCheck,
  Layers,
  Zap,
  BookOpen,
  History,
  Database as DatabaseIcon,
  ExternalLink as ExternalLinkIcon,
  DownloadCloud,
  UploadCloud,
  Globe,
  Send,
  GitBranch,
  Eye,
  Info,
  CheckCircle,
  XCircle
} from 'lucide-react';

// Import CodeBaseController functions
import {
  getCollectionsListFromCodebase,
  getCollectionDetailsFromCodebase,
  getRequestDetailsFromCodebase,
  getImplementationDetails,
  generateImplementation,
  exportImplementation,
  getLanguages,
  searchImplementations,
  importSpecification,
  clearCodebaseCache,
  getAllImplementations,
  validateImplementation,
  testImplementation,
  handleCodebaseResponse,
  extractCodebaseCollectionsList,
  extractCodebaseCollectionDetails,
  extractCodebaseRequestDetails,
  extractImplementationDetails,
  extractGenerateResults,
  extractExportResults,
  extractLanguages,
  extractSearchResults,
  extractImportSpecResults,
  extractValidationResults,
  extractTestResults,
  validateGenerateImplementation,
  validateExportImplementation,
  validateSearchImplementation,
  validateImportSpecification,
  validateImplementationValidation,
  validateTestImplementation,
  getSupportedProgrammingLanguages,
  getSupportedComponents,
  getQuickStartGuide,
  getLanguageColor,
  formatCodeForDisplay,
  getFileExtension,
  getDefaultImplementationOptions,
  cacheCodebaseData,
  getCachedCodebaseData,
  clearCachedCodebaseData,
  getFolderRequestsFromCodebase,
  extractFolderRequests,
  extractAllImplementations,
  extractUserIdFromToken,
  extractSupportedProgrammingLanguages,
  extractQuickStartGuide
} from "../controllers/CodeBaseController.js";

// ==================== PROTOCOL-SPECIFIC CODE GENERATORS ====================

// Helper: Get method color
const getMethodColor = (method) => {
  const colors = {
    GET: '#10b981',
    POST: '#3b82f6',
    PUT: '#f59e0b',
    DELETE: '#ef4444',
    PATCH: '#8b5cf6',
    HEAD: '#6b7280',
    OPTIONS: '#8b5cf6'
  };
  return colors[method] || '#64748b';
};

// Helper: Get protocol icon
const getProtocolIcon = (protocolType) => {
  switch(protocolType?.toLowerCase()) {
    case 'soap':
      return <Send size={12} />;
    case 'graphql':
      return <GitBranch size={12} />;
    default:
      return <Globe size={12} />;
  }
};

// Helper: Get protocol color
const getProtocolColor = (protocolType) => {
  switch(protocolType?.toLowerCase()) {
    case 'soap':
      return '#3b82f6';
    case 'graphql':
      return '#10b981';
    default:
      return '#8b5cf6';
  }
};

// Generate cURL code (bash)
const generateCurlCode = (endpointDetails, baseUrl = '') => {
  const { method, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  // Build headers
  let headerLines = [];
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headerLines.push(`  -H "${header.key}: ${header.value}"`);
      }
    });
  }
  
  // Protocol-specific header additions
  if (protocolType === 'soap') {
    if (!headerLines.some(h => h.includes('Content-Type: text/xml'))) {
      headerLines.push(`  -H "Content-Type: text/xml; charset=utf-8"`);
    }
    const soapAction = headers?.find(h => h.key === 'SOAPAction')?.value;
    if (soapAction) {
      headerLines.push(`  -H "SOAPAction: ${soapAction}"`);
    }
  } else if (protocolType === 'graphql') {
    if (!headerLines.some(h => h.includes('Content-Type: application/json'))) {
      headerLines.push(`  -H "Content-Type: application/json"`);
    }
  } else {
    if (!headerLines.some(h => h.includes('Content-Type: application/json'))) {
      headerLines.push(`  -H "Content-Type: application/json"`);
    }
  }
  
  // Build body
  let bodyString = '';
  if (body) {
    if (protocolType === 'soap') {
      const xmlBody = body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body));
      bodyString = ` \\\n  -d '${xmlBody.replace(/'/g, "'\\''")}'`;
    } else if (protocolType === 'graphql') {
      const graphqlBody = JSON.stringify({ query: body.query, variables: body.variables || {} }, null, 2);
      bodyString = ` \\\n  -d '${graphqlBody.replace(/'/g, "'\\''")}'`;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? JSON.stringify(body, null, 2) : body);
      if (jsonBody && jsonBody !== '') {
        bodyString = ` \\\n  -d '${jsonBody.replace(/'/g, "'\\''")}'`;
      }
    }
  }
  
  const headerString = headerLines.length > 0 ? ` \\\n${headerLines.join(' \\\n')}` : '';
  
  return `curl -X ${method} "${fullUrl}"${headerString}${bodyString}`;
};

// Generate JavaScript/Node.js code
const generateJavaScriptCode = (endpointDetails, baseUrl = '') => {
  const { method, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  // Build headers object
  const headersObj = {};
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headersObj[header.key] = header.value;
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    headersObj['Content-Type'] = 'text/xml; charset=utf-8';
  } else if (protocolType === 'graphql') {
    headersObj['Content-Type'] = 'application/json';
  } else if (!headersObj['Content-Type']) {
    headersObj['Content-Type'] = 'application/json';
  }
  
  // Build body
  let bodyCode = '';
  
  if (body) {
    if (protocolType === 'soap') {
      const xmlBody = body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body));
      bodyCode = `\n  body: \`${xmlBody.replace(/`/g, '\\`')}\`,`;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      bodyCode = `\n  body: JSON.stringify(${JSON.stringify(graphqlBody, null, 2)}),`;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody && typeof jsonBody === 'object') {
        bodyCode = `\n  body: JSON.stringify(${JSON.stringify(jsonBody, null, 2)}),`;
      } else if (jsonBody && typeof jsonBody === 'string') {
        bodyCode = `\n  body: ${JSON.stringify(jsonBody)},`;
      }
    }
  }
  
  const headersString = Object.entries(headersObj)
    .map(([key, value]) => `    "${key}": "${value}"`)
    .join(',\n');
  
  return `// Auto-generated JavaScript code for ${endpointDetails.name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

async function callApi() {
  const url = '${fullUrl}';
  
  const options = {
    method: '${method}',${bodyCode}
    headers: {
${headersString}
    }
  };

  try {
    const response = await fetch(url, options);
    const data = await response.json();
    
    console.log('Status:', response.status);
    console.log('Response:', data);
    
    return data;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

// Example usage
callApi()
  .then(data => console.log('Success:', data))
  .catch(error => console.error('Failed:', error));`;
};

// Generate Python code
const generatePythonCode = (endpointDetails, baseUrl = '') => {
  const { method, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  // Build headers dict
  const headersDict = {};
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headersDict[header.key] = header.value;
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    headersDict['Content-Type'] = 'text/xml; charset=utf-8';
  } else if (protocolType === 'graphql') {
    headersDict['Content-Type'] = 'application/json';
  } else if (!headersDict['Content-Type']) {
    headersDict['Content-Type'] = 'application/json';
  }
  
  // Build body
  let bodyCode = '';
  let requestType = 'json';
  
  if (body) {
    if (protocolType === 'soap') {
      const xmlBody = body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body));
      bodyCode = `data='''${xmlBody}'''`;
      requestType = 'data';
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      bodyCode = `json=${JSON.stringify(graphqlBody, null, 2)}`;
      requestType = 'json';
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody) {
        bodyCode = `json=${JSON.stringify(jsonBody, null, 2)}`;
        requestType = 'json';
      }
    }
  }
  
  const headersString = JSON.stringify(headersDict, null, 2);
  const methodLower = method.toLowerCase();
  
  return `# Auto-generated Python code for ${endpointDetails.name || 'API endpoint'}
# Protocol: ${protocolType?.toUpperCase() || 'REST'}

import requests
import json

def call_api():
    url = '${fullUrl}'
    
    headers = ${headersString}
    
    ${bodyCode ? `${requestType} = ${bodyCode}` : ''}
    
    try:
        response = requests.${methodLower}(url, headers=headers${bodyCode ? `, ${requestType}=${requestType.split('=')[0]}` : ''})
        
        print(f"Status Code: {response.status_code}")
        print("Response:")
        print(json.dumps(response.json(), indent=2))
        
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error: {e}")
        raise

if __name__ == "__main__":
    call_api()`;
};

// Generate Java code
const generateJavaCode = (endpointDetails, baseUrl = '') => {
  const { method, name, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  const className = (name || 'ApiClient')
    .replace(/[^a-zA-Z0-9]/g, ' ')
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join('');
  
  // Build headers
  const headerStatements = [];
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headerStatements.push(`        .header("${header.key}", "${header.value}")`);
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        .header("Content-Type", "text/xml; charset=utf-8")`);
    }
  } else if (protocolType === 'graphql') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        .header("Content-Type", "application/json")`);
    }
  } else if (!headerStatements.some(h => h.includes('Content-Type'))) {
    headerStatements.push(`        .header("Content-Type", "application/json")`);
  }
  
  // Build body
  let bodyCode = '';
  let bodyString = '';
  
  if (body) {
    if (protocolType === 'soap') {
      bodyString = (body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body)))
        .replace(/"/g, '\\"')
        .replace(/\n/g, '\\n');
      bodyCode = `\n        .POST(HttpRequest.BodyPublishers.ofString(requestBody))`;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      bodyString = JSON.stringify(graphqlBody, null, 2).replace(/"/g, '\\"').replace(/\n/g, '\\n');
      bodyCode = `\n        .POST(HttpRequest.BodyPublishers.ofString(requestBody))`;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody) {
        bodyString = JSON.stringify(jsonBody, null, 2).replace(/"/g, '\\"').replace(/\n/g, '\\n');
        bodyCode = `\n        .POST(HttpRequest.BodyPublishers.ofString(requestBody))`;
      } else {
        bodyCode = `\n        .${method}()`;
      }
    }
  } else {
    bodyCode = `\n        .${method}()`;
  }
  
  const headerString = headerStatements.join('');
  
  return `// Auto-generated Java code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ${className} {
    
    private static final String BASE_URL = "${fullUrl}";
    private final HttpClient httpClient;
    
    public ${className}() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    public void callApi() throws Exception {
        ${bodyString ? `String requestBody = "${bodyString}";` : ''}
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))${headerString}${bodyCode}
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
    }
    
    public static void main(String[] args) {
        try {
            new ${className}().callApi();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}`;
};

// Generate Go code
const generateGoCode = (endpointDetails, baseUrl = '') => {
  const { method, name, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  // Build headers
  const headerStatements = [];
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headerStatements.push(`    req.Header.Set("${header.key}", "${header.value}")`);
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`    req.Header.Set("Content-Type", "text/xml; charset=utf-8")`);
    }
  } else if (protocolType === 'graphql') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`    req.Header.Set("Content-Type", "application/json")`);
    }
  } else if (!headerStatements.some(h => h.includes('Content-Type'))) {
    headerStatements.push(`    req.Header.Set("Content-Type", "application/json")`);
  }
  
  // Build body
  let bodyCode = '';
  let bodyVar = '';
  
  if (body) {
    if (protocolType === 'soap') {
      bodyVar = (body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body)))
        .replace(/`/g, '` + "`" + `');
      bodyCode = `\n    jsonData := []byte(\`${bodyVar}\`)`;
      bodyCode += `\n    req, err := http.NewRequest("${method}", url, bytes.NewBuffer(jsonData))`;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      bodyVar = JSON.stringify(graphqlBody, null, 2);
      bodyCode = `\n    jsonData := []byte(\`${bodyVar}\`)`;
      bodyCode += `\n    req, err := http.NewRequest("${method}", url, bytes.NewBuffer(jsonData))`;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody) {
        bodyVar = JSON.stringify(jsonBody, null, 2);
        bodyCode = `\n    jsonData := []byte(\`${bodyVar}\`)`;
        bodyCode += `\n    req, err := http.NewRequest("${method}", url, bytes.NewBuffer(jsonData))`;
      } else {
        bodyCode = `\n    req, err := http.NewRequest("${method}", url, nil)`;
      }
    }
  } else {
    bodyCode = `\n    req, err := http.NewRequest("${method}", url, nil)`;
  }
  
  return `// Auto-generated Go code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

package main

import (
    "bytes"
    "encoding/json"
    "fmt"
    "io"
    "net/http"
)

func main() {
    url := "${fullUrl}"
    ${bodyCode ? bodyCode : `    req, err := http.NewRequest("${method}", url, nil)`}
    if err != nil {
        fmt.Printf("Error creating request: %v\\n", err)
        return
    }
    
${headerStatements.join('\n')}
    
    client := &http.Client{}
    resp, err := client.Do(req)
    if err != nil {
        fmt.Printf("Error making request: %v\\n", err)
        return
    }
    defer resp.Body.Close()
    
    body, err := io.ReadAll(resp.Body)
    if err != nil {
        fmt.Printf("Error reading response: %v\\n", err)
        return
    }
    
    fmt.Printf("Status Code: %d\\n", resp.StatusCode)
    fmt.Printf("Response Body: %s\\n", string(body))
}`;
};

// Generate C# code
const generateCSharpCode = (endpointDetails, baseUrl = '') => {
  const { method, name, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  const className = (name || 'ApiClient')
    .replace(/[^a-zA-Z0-9]/g, ' ')
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join('');
  
  // Build headers
  const headerStatements = [];
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headerStatements.push(`        _httpClient.DefaultRequestHeaders.Add("${header.key}", "${header.value}");`);
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        _httpClient.DefaultRequestHeaders.Add("Content-Type", "text/xml; charset=utf-8");`);
    }
  } else if (protocolType === 'graphql') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        _httpClient.DefaultRequestHeaders.Add("Content-Type", "application/json");`);
    }
  } else if (!headerStatements.some(h => h.includes('Content-Type'))) {
    headerStatements.push(`        _httpClient.DefaultRequestHeaders.Add("Content-Type", "application/json");`);
  }
  
  // Build body
  let bodyCode = '';
  let httpMethod = method === 'GET' ? 'GetAsync' : method === 'POST' ? 'PostAsync' : method === 'PUT' ? 'PutAsync' : 'DeleteAsync';
  
  if (body && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
    let bodyString = '';
    if (protocolType === 'soap') {
      bodyString = (body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body)))
        .replace(/"/g, '\\"');
      bodyCode = `\n        var content = new StringContent("${bodyString}", Encoding.UTF8, "text/xml");`;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      bodyString = JSON.stringify(graphqlBody, null, 2).replace(/"/g, '\\"');
      bodyCode = `\n        var content = new StringContent("${bodyString}", Encoding.UTF8, "application/json");`;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody) {
        bodyString = JSON.stringify(jsonBody, null, 2).replace(/"/g, '\\"');
        bodyCode = `\n        var content = new StringContent("${bodyString}", Encoding.UTF8, "application/json");`;
      }
    }
  }
  
  return `// Auto-generated C# code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

using System;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

public class ${className}
{
    private static readonly string BaseUrl = "${fullUrl}";
    private readonly HttpClient _httpClient;
    
    public ${className}()
    {
        _httpClient = new HttpClient();
${headerStatements.join('\n')}
    }
    
    public async Task CallApiAsync()
    {
        ${bodyCode ? bodyCode.replace('\n', '\n        ') : ''}
        
        try
        {
            HttpResponseMessage response;
            ${bodyCode ? `response = await _httpClient.${httpMethod}(BaseUrl, content);` : `response = await _httpClient.${httpMethod}(BaseUrl);`}
            
            string responseBody = await response.Content.ReadAsStringAsync();
            Console.WriteLine($"Status Code: {(int)response.StatusCode}");
            Console.WriteLine($"Response Body: {responseBody}");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error: {ex.Message}");
        }
    }
    
    public static async Task Main(string[] args)
    {
        var client = new ${className}();
        await client.CallApiAsync();
    }
}`;
};



// Generate TypeScript code
const generateTypeScriptCode = (endpointDetails, baseUrl = '') => {
  const { method, name, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  // Build headers object
  const headersObj = {};
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headersObj[header.key] = header.value;
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    headersObj['Content-Type'] = 'text/xml; charset=utf-8';
  } else if (protocolType === 'graphql') {
    headersObj['Content-Type'] = 'application/json';
  } else if (!headersObj['Content-Type']) {
    headersObj['Content-Type'] = 'application/json';
  }
  
  // Build body
  let bodyCode = '';
  let hasBody = false;
  
  if (body) {
    if (protocolType === 'soap') {
      const xmlBody = body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body));
      bodyCode = `\n  body: \`${xmlBody.replace(/`/g, '\\`')}\`,`;
      hasBody = true;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      bodyCode = `\n  body: JSON.stringify(${JSON.stringify(graphqlBody, null, 2)}),`;
      hasBody = true;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody && typeof jsonBody === 'object') {
        bodyCode = `\n  body: JSON.stringify(${JSON.stringify(jsonBody, null, 2)}),`;
        hasBody = true;
      } else if (jsonBody && typeof jsonBody === 'string') {
        bodyCode = `\n  body: ${JSON.stringify(jsonBody)},`;
        hasBody = true;
      }
    }
  }
  
  const headersString = Object.entries(headersObj)
    .map(([key, value]) => `    "${key}": "${value}"`)
    .join(',\n');
  
  const className = (name || 'ApiClient')
    .replace(/[^a-zA-Z0-9]/g, ' ')
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join('');
  
  if (hasBody) {
    return `// Auto-generated TypeScript code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

interface ApiResponse<T = any> {
  data: T;
  status: number;
  statusText: string;
}

interface ApiOptions {
  method: string;
  headers: Record<string, string>;
  body?: string;
}

class ${className} {
  private baseUrl: string;
  
  constructor(baseUrl: string = '${fullUrl}') {
    this.baseUrl = baseUrl;
  }
  
  async callApi<T = any>(requestBody: any): Promise<ApiResponse<T>> {
    const url = this.baseUrl;
    
    const options: ApiOptions = {
      method: '${method}',
      headers: {
${headersString}
      },${bodyCode}
    };
    
    try {
      const response = await fetch(url, options);
      const data = await response.json();
      
      console.log('Status:', response.status);
      console.log('Response:', data);
      
      return {
        data: data as T,
        status: response.status,
        statusText: response.statusText
      };
    } catch (error) {
      console.error('Error:', error);
      throw error;
    }
  }
}

// Example usage
const client = new ${className}();
client.callApi({})
  .then(response => console.log('Success:', response.data))
  .catch(error => console.error('Failed:', error));`;
  } else {
    return `// Auto-generated TypeScript code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

interface ApiResponse<T = any> {
  data: T;
  status: number;
  statusText: string;
}

interface ApiOptions {
  method: string;
  headers: Record<string, string>;
}

class ${className} {
  private baseUrl: string;
  
  constructor(baseUrl: string = '${fullUrl}') {
    this.baseUrl = baseUrl;
  }
  
  async callApi<T = any>(): Promise<ApiResponse<T>> {
    const url = this.baseUrl;
    
    const options: ApiOptions = {
      method: '${method}',
      headers: {
${headersString}
      }
    };
    
    try {
      const response = await fetch(url, options);
      const data = await response.json();
      
      console.log('Status:', response.status);
      console.log('Response:', data);
      
      return {
        data: data as T,
        status: response.status,
        statusText: response.statusText
      };
    } catch (error) {
      console.error('Error:', error);
      throw error;
    }
  }
}

// Example usage
const client = new ${className}();
client.callApi()
  .then(response => console.log('Success:', response.data))
  .catch(error => console.error('Failed:', error));`;
  }
};

// Generate Kotlin code
const generateKotlinCode = (endpointDetails, baseUrl = '') => {
  const { method, name, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  const className = (name || 'ApiClient')
    .replace(/[^a-zA-Z0-9]/g, ' ')
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join('');
  
  // Build headers
  const headerStatements = [];
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headerStatements.push(`        .header("${header.key}", "${header.value}")`);
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        .header("Content-Type", "text/xml; charset=utf-8")`);
    }
  } else if (protocolType === 'graphql') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        .header("Content-Type", "application/json")`);
    }
  } else if (!headerStatements.some(h => h.includes('Content-Type'))) {
    headerStatements.push(`        .header("Content-Type", "application/json")`);
  }
  
  // Build body
  let bodyCode = '';
  let bodyString = '';
  let hasBody = false;
  
  if (body) {
    if (protocolType === 'soap') {
      bodyString = (body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body)))
        .replace(/"/g, '\\"')
        .replace(/\n/g, '\\n');
      bodyCode = `\n        .POST(HttpRequest.BodyPublishers.ofString(requestBody))`;
      hasBody = true;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      bodyString = JSON.stringify(graphqlBody, null, 2).replace(/"/g, '\\"').replace(/\n/g, '\\n');
      bodyCode = `\n        .POST(HttpRequest.BodyPublishers.ofString(requestBody))`;
      hasBody = true;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody) {
        bodyString = JSON.stringify(jsonBody, null, 2).replace(/"/g, '\\"').replace(/\n/g, '\\n');
        bodyCode = `\n        .POST(HttpRequest.BodyPublishers.ofString(requestBody))`;
        hasBody = true;
      } else {
        bodyCode = `\n        .${method.toUpperCase()}()`;
      }
    }
  } else {
    bodyCode = `\n        .${method.toUpperCase()}()`;
  }
  
  const headerString = headerStatements.join('');
  
  if (hasBody) {
    return `// Auto-generated Kotlin code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class ${className} {
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build()
    
    private val baseUrl = "${fullUrl}"
    
    fun callApi(requestBody: String = "${bodyString}"): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl))${headerString}${bodyCode}
            .build()
        
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        
        println("Status Code: \${response.statusCode()}")
        println("Response Body: \${response.body()}")
        
        return response
    }
}

fun main() {
    val client = ${className}()
    val response = client.callApi()
}`;
  } else {
    return `// Auto-generated Kotlin code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class ${className} {
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build()
    
    private val baseUrl = "${fullUrl}"
    
    fun callApi(): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl))${headerString}${bodyCode}
            .build()
        
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        
        println("Status Code: \${response.statusCode()}")
        println("Response Body: \${response.body()}")
        
        return response
    }
}

fun main() {
    val client = ${className}()
    val response = client.callApi()
}`;
  }
};

// Generate Swift code
const generateSwiftCode = (endpointDetails, baseUrl = '') => {
  const { method, name, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  // Clean up className
  let className = (name || 'ApiClient')
    .replace(/[^a-zA-Z0-9]/g, ' ')
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join('');
  
  if (className.length === 0) className = 'ApiClient';
  
  // Build headers array
  const headerStatements = [];
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headerStatements.push(`        request.setValue("${header.value}", forHTTPHeaderField: "${header.key}")`);
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        request.setValue("text/xml; charset=utf-8", forHTTPHeaderField: "Content-Type")`);
    }
  } else if (protocolType === 'graphql') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        request.setValue("application/json", forHTTPHeaderField: "Content-Type")`);
    }
  } else if (!headerStatements.some(h => h.includes('Content-Type'))) {
    headerStatements.push(`        request.setValue("application/json", forHTTPHeaderField: "Content-Type")`);
  }
  
  // Build body
  let bodyCode = '';
  let bodyString = '';
  
  if (body) {
    if (protocolType === 'soap') {
      bodyString = (body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body)))
        .replace(/"/g, '\\"');
      bodyCode = `\n        request.httpBody = "${bodyString}".data(using: .utf8)`;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      bodyString = JSON.stringify(graphqlBody, null, 2).replace(/"/g, '\\"');
      bodyCode = `\n        request.httpBody = "${bodyString}".data(using: .utf8)`;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody) {
        bodyString = JSON.stringify(jsonBody, null, 2).replace(/"/g, '\\"');
        bodyCode = `\n        request.httpBody = "${bodyString}".data(using: .utf8)`;
      }
    }
  }
  
  const headerString = headerStatements.join('\n');
  
  return `// Auto-generated Swift code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

import Foundation

class ${className} {
    private let baseURL = URL(string: "${fullUrl}")!
    
    func callApi(completion: @escaping (Data?, URLResponse?, Error?) -> Void) {
        var request = URLRequest(url: baseURL)
        request.httpMethod = "${method}"
${headerString}${bodyCode}
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("Error: \\(error)")
                completion(nil, nil, error)
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                print("Status Code: \\(httpResponse.statusCode)")
            }
            
            if let data = data {
                print("Response Body: \\(String(data: data, encoding: .utf8) ?? "")")
                completion(data, response, nil)
            }
        }
        
        task.resume()
    }
}

// Example usage
let client = ${className}()
client.callApi { data, response, error in
    if let data = data {
        print("Success: \\(data)")
    }
}`;
};

// Generate Rust code
const generateRustCode = (endpointDetails, baseUrl = '') => {
  const { method, name, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  // Build headers array
  const headerStatements = [];
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headerStatements.push(`        .header("${header.key}", "${header.value}")`);
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        .header("Content-Type", "text/xml; charset=utf-8")`);
    }
  } else if (protocolType === 'graphql') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`        .header("Content-Type", "application/json")`);
    }
  } else if (!headerStatements.some(h => h.includes('Content-Type'))) {
    headerStatements.push(`        .header("Content-Type", "application/json")`);
  }
  
  // Build body
  let bodyCode = '';
  let bodyString = '';
  
  if (body) {
    if (protocolType === 'soap') {
      bodyString = (body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body)))
        .replace(/"/g, '\\"');
      bodyCode = `\n        .body(r#"${bodyString}"#)`;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      bodyString = JSON.stringify(graphqlBody, null, 2);
      bodyCode = `\n        .json(&${bodyString.replace(/"/g, '\\"')})`;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody) {
        bodyString = JSON.stringify(jsonBody, null, 2);
        bodyCode = `\n        .json(&${bodyString.replace(/"/g, '\\"')})`;
      }
    }
  }
  
  const headerString = headerStatements.join('\n');
  const methodLower = method.toLowerCase();
  
  return `// Auto-generated Rust code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

use reqwest;
use serde_json::json;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let client = reqwest::Client::new();
    let url = "${fullUrl}";
    
    let request = client.${methodLower}(url)${headerString}${bodyCode};
    
    match request.send().await {
        Ok(response) => {
            println!("Status: {}", response.status());
            
            let body = response.text().await?;
            println!("Response Body: {}", body);
        }
        Err(e) => {
            println!("Error: {}", e);
        }
    }
    
    Ok(())
}`;
};



// Generate PHP code (FIXED: removed json_encode)
const generatePhpCode = (endpointDetails, baseUrl = '') => {
  const { method, name, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  // Build headers array
  const headerArray = [];
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headerArray.push(`        '${header.key}: ${header.value}'`);
      }
    });
  }
  
  // Protocol-specific headers
  let contentType = 'application/json';
  if (protocolType === 'soap') {
    contentType = 'text/xml; charset=utf-8';
  } else if (protocolType === 'graphql') {
    contentType = 'application/json';
  }
  
  if (!headerArray.some(h => h.includes('Content-Type'))) {
    headerArray.push(`        'Content-Type: ${contentType}'`);
  }
  
  // Build body
  let bodyCode = '';
  let bodyVar = '';
  
  if (body && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
    if (protocolType === 'soap') {
      bodyVar = (body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body)))
        .replace(/'/g, "\\'");
      bodyCode = `\n    curl_setopt($ch, CURLOPT_POSTFIELDS, '${bodyVar}');`;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      const bodyJson = JSON.stringify(graphqlBody, null, 2);
      bodyVar = bodyJson.replace(/'/g, "\\'");
      bodyCode = `\n    $data = json_decode('${bodyVar}', true);`;
      bodyCode += `\n    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));`;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody) {
        const bodyJson = JSON.stringify(jsonBody, null, 2);
        bodyVar = bodyJson.replace(/'/g, "\\'");
        bodyCode = `\n    $data = json_decode('${bodyVar}', true);`;
        bodyCode += `\n    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));`;
      }
    }
  }
  
  const headerString = headerArray.join(",\n");
  
  return `<?php
// Auto-generated PHP code for ${name || 'API endpoint'}
// Protocol: ${protocolType?.toUpperCase() || 'REST'}

function callApi() {
    $url = '${fullUrl}';
    
    $ch = curl_init($url);
    
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, '${method}');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
${headerString}
    ));${bodyCode}
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    
    curl_close($ch);
    
    echo "Status Code: $httpCode\\n";
    
    if ($error) {
        echo "cURL Error: $error\\n";
        return null;
    }
    
    $decodedResponse = json_decode($response, true);
    if (json_last_error() === JSON_ERROR_NONE) {
        echo "Response Body:\\n";
        print_r($decodedResponse);
        return $decodedResponse;
    } else {
        echo "Response Body: $response\\n";
        return $response;
    }
}

// Example usage
$result = callApi();
?>`;
};

// Generate Ruby code
const generateRubyCode = (endpointDetails, baseUrl = '') => {
  const { method, name, url, protocolType, headers, body } = endpointDetails;
  
  let fullUrl = url || '';
  if (baseUrl && fullUrl.includes('{{baseUrl}}')) {
    fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
  }
  
  const className = (name || 'ApiClient')
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join('');
  
  // Build headers
  const headerStatements = [];
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      if (header.key && !header.disabled) {
        headerStatements.push(`  request['${header.key}'] = '${header.value}'`);
      }
    });
  }
  
  // Protocol-specific headers
  if (protocolType === 'soap') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`  request['Content-Type'] = 'text/xml; charset=utf-8'`);
    }
  } else if (protocolType === 'graphql') {
    if (!headerStatements.some(h => h.includes('Content-Type'))) {
      headerStatements.push(`  request['Content-Type'] = 'application/json'`);
    }
  } else if (!headerStatements.some(h => h.includes('Content-Type'))) {
    headerStatements.push(`  request['Content-Type'] = 'application/json'`);
  }
  
  // Build body
  let bodyCode = '';
  
  if (body && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
    if (protocolType === 'soap') {
      const xmlBody = (body.sample || body.xml || (typeof body === 'string' ? body : JSON.stringify(body)))
        .replace(/'/g, "\\\\'");
      bodyCode = `\n  request.body = '${xmlBody}'`;
    } else if (protocolType === 'graphql') {
      const graphqlBody = { query: body.query, variables: body.variables || {} };
      const bodyJson = JSON.stringify(graphqlBody, null, 2);
      bodyCode = `\n  request.body = '${bodyJson.replace(/'/g, "\\\\'")}'`;
    } else {
      const jsonBody = body.sample || (typeof body === 'object' ? body : null);
      if (jsonBody) {
        const bodyJson = JSON.stringify(jsonBody, null, 2);
        bodyCode = `\n  request.body = '${bodyJson.replace(/'/g, "\\\\'")}'`;
      }
    }
  }
  
  const headerString = headerStatements.join('\n');
  
  return `# Auto-generated Ruby code for ${name || 'API endpoint'}
# Protocol: ${protocolType?.toUpperCase() || 'REST'}

require 'net/http'
require 'uri'
require 'json'

class ${className}
  def initialize
    @uri = URI('${fullUrl}')
  end
  
  def call_api
    http = Net::HTTP.new(@uri.host, @uri.port)
    http.use_ssl = @uri.scheme == 'https'
    
    request = Net::HTTP::${method.capitalize}.new(@uri)
${headerString ? "\n" + headerString : ''}${bodyCode}
    
    response = http.request(request)
    
    puts "Status Code: \#{response.code}"
    puts "Response Body: \#{response.body}"
    
    JSON.parse(response.body) rescue response.body
  end
end

# Example usage
client = ${className}.new
result = client.call_api
puts "Result: \#{result}"`;
};

// Main SyntaxHighlighter Component
const SyntaxHighlighter = ({ language, code }) => {
  if (!code) return <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed">// No code available</pre>;
  
  const highlightCode = (code, lang) => {
    const lines = String(code).split('\n');
    
    return lines.map((line, lineIndex) => {
      let highlightedLine = line
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
      
      if (lang === 'json') {
        highlightedLine = highlightedLine.replace(/"([^"\\]*(\\.[^"\\]*)*)"(\s*:)/g, 
          '<span class="text-blue-400">"$1"</span>$3');
        highlightedLine = highlightedLine.replace(/:(\s*)"([^"\\]*(\\.[^"\\]*)*)"/g, 
          ':<span class="text-green-400">"$2"</span>');
        highlightedLine = highlightedLine.replace(/:(\s*)(\d+)([,\n])/g, 
          ':<span class="text-orange-400">$2</span>$3');
        highlightedLine = highlightedLine.replace(/\b(true|false|null)\b/g, 
          '<span class="text-purple-400">$1</span>');
      } else if (lang === 'xml' || lang === 'soap') {
        highlightedLine = highlightedLine.replace(/(&lt;\/?[a-zA-Z:][^&gt;]*&gt;)/g, 
          '<span class="text-blue-400">$1</span>');
        highlightedLine = highlightedLine.replace(/\b([a-zA-Z:]+)=/g, 
          '<span class="text-orange-400">$1</span>=');
        highlightedLine = highlightedLine.replace(/=("([^"]*)")/g, 
          '=<span class="text-green-400">$1</span>');
      } else if (lang === 'graphql') {
        highlightedLine = highlightedLine.replace(/(query|mutation|subscription|fragment|on|type|interface|enum|scalar|input|extend|directive|schema)\b/g, 
          '<span class="text-purple-400">$1</span>');
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)")/g, 
          '<span class="text-green-400">$1</span>');
        highlightedLine = highlightedLine.replace(/\b(true|false|null)\b/g, 
          '<span class="text-orange-400">$1</span>');
        highlightedLine = highlightedLine.replace(/\$(\w+)/g, 
          '<span class="text-yellow-400">$$1</span>');
      } else if (lang === 'javascript' || lang === 'nodejs') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)"|'([^'\\]*(\\.[^'\\]*)*)')/g, 
          '<span class="text-green-400">$1</span>');
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        const jsKeywords = ['function', 'const', 'let', 'var', 'if', 'else', 'for', 'while', 'return', 'class', 'import', 'export', 'from', 'default', 'async', 'await', 'try', 'catch', 'finally', 'throw', 'new', 'this', 'true', 'false', 'null', 'undefined'];
        jsKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, '<span class="text-purple-400">$1</span>');
        });
      } else if (lang === 'python') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)"|'([^'\\]*(\\.[^'\\]*)*)')/g, 
          '<span class="text-green-400">$1</span>');
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(#.*)/g, '<span class="text-gray-500">$1</span>');
        }
        const pythonKeywords = ['def', 'class', 'import', 'from', 'if', 'elif', 'else', 'for', 'while', 'try', 'except', 'finally', 'with', 'as', 'return', 'yield', 'async', 'await', 'lambda', 'in', 'is', 'not', 'and', 'or', 'True', 'False', 'None'];
        pythonKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, '<span class="text-purple-400">$1</span>');
        });
      } else if (lang === 'java') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)")/g, 
          '<span class="text-green-400">$1</span>');
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        const javaKeywords = ['public', 'private', 'protected', 'class', 'interface', 'extends', 'implements', 'static', 'final', 'void', 'return', 'new', 'if', 'else', 'for', 'while', 'try', 'catch', 'finally', 'throw', 'throws', 'import', 'package', 'true', 'false', 'null'];
        javaKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, '<span class="text-purple-400">$1</span>');
        });
      } else if (lang === 'go') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)")/g, 
          '<span class="text-green-400">$1</span>');
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        const goKeywords = ['package', 'import', 'func', 'var', 'const', 'type', 'struct', 'interface', 'map', 'chan', 'return', 'if', 'else', 'for', 'range', 'switch', 'case', 'default', 'break', 'continue', 'goto', 'defer', 'go', 'select', 'true', 'false', 'nil'];
        goKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, '<span class="text-purple-400">$1</span>');
        });
      } else if (lang === 'csharp') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)")/g, 
          '<span class="text-green-400">$1</span>');
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        const csharpKeywords = ['using', 'namespace', 'class', 'interface', 'struct', 'enum', 'public', 'private', 'protected', 'internal', 'static', 'void', 'return', 'new', 'if', 'else', 'for', 'foreach', 'while', 'try', 'catch', 'finally', 'throw', 'true', 'false', 'null'];
        csharpKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, '<span class="text-purple-400">$1</span>');
        });
      } else if (lang === 'php') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)")/g, 
          '<span class="text-green-400">$1</span>');
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*|\/\*.*\*\/)/g, 
            '<span class="text-gray-500">$1</span>');
        }
      } else if (lang === 'ruby') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)")/g, 
          '<span class="text-green-400">$1</span>');
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(#.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
      }
      
      return <div key={lineIndex} dangerouslySetInnerHTML={{ __html: highlightedLine || '&nbsp;' }} />;
    });
  };

  return (
    <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed">
      {highlightCode(code, language)}
    </pre>
  );
};

// Protocol Badge Component
const ProtocolBadge = ({ protocol, size = 'sm', colors }) => {
  const getProtocolConfig = () => {
    switch (protocol?.toLowerCase()) {
      case 'soap':
        return { icon: Send, label: 'SOAP', bgColor: `${colors.info}20`, textColor: colors.info };
      case 'graphql':
        return { icon: GitBranch, label: 'GraphQL', bgColor: `${colors.success}20`, textColor: colors.success };
      default:
        return { icon: Code, label: 'REST', bgColor: `${colors.primary}20`, textColor: colors.primary };
    }
  };
  
  const config = getProtocolConfig();
  const Icon = config.icon;
  const sizeClasses = size === 'sm' ? 'text-xs px-2 py-0.5 gap-1' : 'text-sm px-3 py-1 gap-1.5';
  const iconSize = size === 'sm' ? 12 : 14;
  
  return (
    <span className={`inline-flex items-center rounded-full ${sizeClasses}`} style={{ backgroundColor: config.bgColor, color: config.textColor }}>
      <Icon size={iconSize} />
      <span>{config.label}</span>
    </span>
  );
};

// Loading Overlay Component
const LoadingOverlay = ({ isLoading, colors, loadingText }) => {
  if (!isLoading) return null;
  
  return (
    <div className="fixed inset-0 flex items-center justify-center z-50" style={{ backgroundColor: colors.bg }}>
      <div className="text-center">
        <div className="relative">
          <Loader className="animate-spin mx-auto mb-6" size={64} style={{ color: colors.primary }} />
          <div className="absolute inset-0 flex items-center justify-center">
            <Code size={32} style={{ color: colors.primary, opacity: 0.3 }} />
          </div>
        </div>
        <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>
          Loading Code Base
        </h3>
        <p className="text-xs mb-2" style={{ color: colors.textSecondary }}>
          {loadingText || 'Please wait while we prepare your code examples'}
        </p>
      </div>
    </div>
  );
};

// Helper function for alphabetical sorting
const sortAlphabetically = (items, key = 'name') => {
  if (!items || !Array.isArray(items)) return [];
  return [...items].sort((a, b) => {
    const nameA = (a[key] || '').toLowerCase();
    const nameB = (b[key] || '').toLowerCase();
    if (nameA < nameB) return -1;
    if (nameA > nameB) return 1;
    return 0;
  });
};

// ==================== MAIN CODEBASE COMPONENT ====================

const CodeBase = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  const [activeTab, setActiveTab] = useState('implementations');
  const [showCodePanel, setShowCodePanel] = useState(true);
  const [selectedLanguage, setSelectedLanguage] = useState('javascript');
  const [showLanguageDropdown, setShowLanguageDropdown] = useState(false);
  const [toast, setToast] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCollection, setSelectedCollection] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [selectedRequestDetails, setSelectedRequestDetails] = useState(null);
  const [isGeneratingCode, setIsGeneratingCode] = useState(false);
  const [collections, setCollections] = useState([]);
  const [expandedCollections, setExpandedCollections] = useState([]);
  const [expandedFolders, setExpandedFolders] = useState([]);
  const [showImportModal, setShowImportModal] = useState(false);
  const [selectedComponent, setSelectedComponent] = useState('controller');
  const [showAllFiles, setShowAllFiles] = useState(false);
  const [isLoading, setIsLoading] = useState({
    collections: false,
    requestDetails: false,
    implementationDetails: false,
    generateImplementation: false,
    searchImplementations: false,
    folderRequests: {},
    initialLoad: true
  });
  const [currentImplementation, setCurrentImplementation] = useState({});
  const [availableLanguages] = useState([
  { id: 'curl', name: 'cURL', icon: <Terminal size={14} />, framework: 'Command Line', color: '#0f172a' },
  { id: 'javascript', name: 'JavaScript', icon: <FileCode size={14} />, framework: 'Node.js/Express', color: '#f7df1e' },
  { id: 'typescript', name: 'TypeScript', icon: <FileCode size={14} />, framework: 'Node.js/Express', color: '#3178c6' },
  { id: 'python', name: 'Python', icon: <Code size={14} />, framework: 'Requests/FastAPI', color: '#3572A5' },
  { id: 'java', name: 'Java', icon: <Coffee size={14} />, framework: 'HttpClient/Spring', color: '#f89820' },
  { id: 'kotlin', name: 'Kotlin', icon: <Cpu size={14} />, framework: 'Ktor/Spring', color: '#7F52FF' },
  { id: 'go', name: 'Go', icon: <Terminal size={14} />, framework: 'net/http/Gin', color: '#00ADD8' },
  { id: 'rust', name: 'Rust', icon: <HardDrive size={14} />, framework: 'Reqwest/Actix', color: '#dea584' },
  { id: 'csharp', name: 'C#', icon: <Box size={14} />, framework: 'HttpClient/.NET', color: '#178600' },
  { id: 'swift', name: 'Swift', icon: <Monitor size={14} />, framework: 'URLSession/Vapor', color: '#F05138' },
  { id: 'php', name: 'PHP', icon: <Package size={14} />, framework: 'cURL/Guzzle', color: '#4F5D95' },
  { id: 'ruby', name: 'Ruby', icon: <Server size={14} />, framework: 'Net::HTTP/Rails', color: '#701516' }
]);
  
  const [folderRequests, setFolderRequests] = useState({});
  const [userId, setUserId] = useState('');
  
  // Refs
  const isFirstLoad = useRef(true);
  const globalLoadingRef = useRef(false);
  const [globalLoading, setGlobalLoading] = useState(false);
  const autoExpandTriggered = useRef(false);
  const selectedCollectionRef = useRef(null);

  // Color scheme
  const colors = isDark ? {
    bg: 'rgb(1 14 35)',
    white: '#FFFFFF',
    sidebar: 'rgb(41 53 72 / 19%)',
    main: 'rgb(1 14 35)',
    header: 'rgb(20 26 38)',
    card: 'rgb(41 53 72 / 19%)',
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    border: 'rgb(51 65 85 / 19%)',
    borderLight: 'rgb(45 55 72)',
    borderDark: 'rgb(71 85 105)',
    hover: 'rgb(45 46 72 / 33%)',
    active: 'rgb(59 74 99)',
    selected: 'rgb(44 82 130)',
    primary: 'rgb(96 165 250)',
    primaryLight: 'rgb(147 197 253)',
    primaryDark: 'rgb(37 99 235)',
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    inputBg: 'rgb(41 53 72 / 19%)',
    dropdownBg: 'rgb(41 53 72 / 19%)',
    modalBg: 'rgb(41 53 72 / 19%)',
    codeBg: 'rgb(41 53 72 / 19%)',
    tableHeader: 'rgb(41 53 72 / 19%)'
  } : {
    bg: '#f8fafc',
    white: '#f8fafc',
    sidebar: '#ffffff',
    main: '#f8fafc',
    header: '#ffffff',
    card: '#ffffff',
    text: '#1e293b',
    textSecondary: '#64748b',
    textTertiary: '#94a3b8',
    border: '#e2e8f0',
    borderLight: '#f1f5f9',
    borderDark: '#cbd5e1',
    hover: '#f1f5f9',
    active: '#e2e8f0',
    selected: '#dbeafe',
    primary: '#1e293b',
    primaryLight: '#60a5fa',
    primaryDark: '#2563eb',
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#3b82f6',
    inputBg: '#ffffff',
    dropdownBg: '#ffffff',
    modalBg: '#ffffff',
    codeBg: '#f1f5f9',
    tableHeader: '#f8fafc'
  };

  // Helper functions
  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const copyToClipboard = useCallback((text) => {
    if (!text) {
      showToast('No code to copy', 'warning');
      return;
    }
    
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text)
        .then(() => showToast('Copied to clipboard!', 'success'))
        .catch(() => fallbackCopy(text));
    } else {
      fallbackCopy(text);
    }
    
    function fallbackCopy(text) {
      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.style.position = 'fixed';
      textarea.style.top = '-9999px';
      textarea.style.left = '-9999px';
      document.body.appendChild(textarea);
      textarea.select();
      try {
        document.execCommand('copy');
        showToast('Copied to clipboard!', 'success');
      } catch (err) {
        showToast('Failed to copy', 'error');
      }
      document.body.removeChild(textarea);
    }
  }, []);

  const withGlobalLoading = async (asyncFn) => {
    if (globalLoadingRef.current) return;
    try {
      globalLoadingRef.current = true;
      setGlobalLoading(true);
      await asyncFn();
    } finally {
      globalLoadingRef.current = false;
      setGlobalLoading(false);
    }
  };

  const hasEndpoints = useCallback((item) => {
    if (item.requests && Array.isArray(item.requests) && item.requests.length > 0) return true;
    if (item.requestCount > 0) return true;
    if (item.folders && Array.isArray(item.folders)) {
      return item.folders.some(folder => hasEndpoints(folder));
    }
    return false;
  }, []);

  // Generate code based on selected language and request details
 const generateCodeExample = useCallback(() => {
  if (!selectedRequestDetails) {
    return '// Select an endpoint to view code examples';
  }
  
  const baseUrl = ''; // Can be configured from environment
  
  switch (selectedLanguage) {
    case 'curl':
      return generateCurlCode(selectedRequestDetails, baseUrl);
    case 'javascript':
      return generateJavaScriptCode(selectedRequestDetails, baseUrl);
    case 'typescript':
      return generateTypeScriptCode(selectedRequestDetails, baseUrl);
    case 'python':
      return generatePythonCode(selectedRequestDetails, baseUrl);
    case 'java':
      return generateJavaCode(selectedRequestDetails, baseUrl);
    case 'kotlin':
      return generateKotlinCode(selectedRequestDetails, baseUrl);
    case 'go':
      return generateGoCode(selectedRequestDetails, baseUrl);
    case 'rust':
      return generateRustCode(selectedRequestDetails, baseUrl);
    case 'csharp':
      return generateCSharpCode(selectedRequestDetails, baseUrl);
    case 'swift':
      return generateSwiftCode(selectedRequestDetails, baseUrl);
    case 'php':
      return generatePhpCode(selectedRequestDetails, baseUrl);
    case 'ruby':
      return generateRubyCode(selectedRequestDetails, baseUrl);
    default:
      return generateCurlCode(selectedRequestDetails, baseUrl);
  }
}, [selectedRequestDetails, selectedLanguage]);

  // Fetch collections from codebase
  const fetchCollectionsList = useCallback(async () => {
    if (!authToken) {
      showToast('Authentication required. Please login.', 'error');
      return;
    }

    setIsLoading(prev => ({ ...prev, collections: true }));

    try {
      const response = await getCollectionsListFromCodebase(authToken);
      const handledResponse = handleCodebaseResponse(response);
      const collectionsData = extractCodebaseCollectionsList(handledResponse);
      
      const formattedCollections = collectionsData.map(collection => ({
        id: collection.id,
        name: collection.name,
        description: collection.description,
        folders: []
      }));
      
      const sortedCollections = sortAlphabetically(formattedCollections, 'name');
      setCollections(sortedCollections);
      
      if (sortedCollections.length > 0) {
        await loadAllCollectionDetails(sortedCollections);
      }
      
      showToast('Collections loaded successfully', 'success');
      
    } catch (error) {
      console.error('Error fetching collections:', error);
      showToast(`Failed to load collections: ${error.message}`, 'error');
      setCollections([]);
    } finally {
      setIsLoading(prev => ({ ...prev, collections: false, initialLoad: false }));
    }
  }, [authToken]);

  // Load details for all collections
  const loadAllCollectionDetails = useCallback(async (basicCollections) => {
    if (!authToken) return;

    try {
      const collectionsWithDetails = await Promise.all(
        basicCollections.map(async (collection) => {
          try {
            const response = await getCollectionDetailsFromCodebase(authToken, collection.id);
            const handledResponse = handleCodebaseResponse(response);
            const details = extractCodebaseCollectionDetails(handledResponse);
            
            if (details) {
              const processedFolders = (details.folders || []).map(folder => ({
                id: folder.id || folder.folderId,
                name: folder.name || folder.folderName,
                description: folder.description || '',
                requests: (folder.requests || []).map(req => ({
                  id: req.id || req.requestId,
                  name: req.name || req.requestName,
                  method: req.method,
                  url: req.url,
                  description: req.description,
                  protocolType: req.protocolType || 'rest',
                  headers: req.headers || [],
                  body: req.body,
                  responseExample: req.responseExample,
                  lastModified: req.lastModified,
                  tags: req.tags || []
                })),
                requestCount: folder.requests ? folder.requests.length : 0
              }));
              
              const sortedFolders = sortAlphabetically(processedFolders, 'name');
              
              const folderRequestsMap = {};
              sortedFolders.forEach(folder => {
                if (folder.requests.length > 0) {
                  folderRequestsMap[folder.id] = folder.requests;
                }
              });
              
              if (Object.keys(folderRequestsMap).length > 0) {
                setFolderRequests(prev => ({ ...prev, ...folderRequestsMap }));
              }
              
              return {
                ...collection,
                folders: sortedFolders,
                totalEndpoints: sortedFolders.reduce((sum, f) => sum + f.requests.length, 0)
              };
            }
            return collection;
          } catch (error) {
            console.error(`Error loading details for collection ${collection.id}:`, error);
            return collection;
          }
        })
      );
      
      const sortedCollections = sortAlphabetically(collectionsWithDetails, 'name');
      setCollections(sortedCollections);
      
      if (sortedCollections.length > 0 && !selectedCollection) {
        const firstCollection = sortedCollections[0];
        setSelectedCollection(firstCollection);
        setExpandedCollections([firstCollection.id]);
      }
      
    } catch (error) {
      console.error('Error loading collection details:', error);
    }
  }, [authToken, selectedCollection]);

  // Fetch collection details
  const fetchCollectionDetails = useCallback(async (collectionId) => {
    if (!authToken || !collectionId) return;

    try {
      const response = await getCollectionDetailsFromCodebase(authToken, collectionId);
      const handledResponse = handleCodebaseResponse(response);
      const details = extractCodebaseCollectionDetails(handledResponse);
      
      if (details) {
        const foldersWithRequests = (details.folders || []).map(folder => ({
          id: folder.id || folder.folderId,
          name: folder.name || folder.folderName,
          description: folder.description,
          requests: (folder.requests || []).map(req => ({
            id: req.id || req.requestId,
            name: req.name || req.requestName,
            method: req.method,
            url: req.url,
            description: req.description,
            protocolType: req.protocolType || 'rest',
            headers: req.headers || [],
            body: req.body,
            responseExample: req.responseExample,
            lastModified: req.lastModified,
            tags: req.tags || []
          })),
          requestCount: folder.requests ? folder.requests.length : 0
        }));
        
        const sortedFolders = sortAlphabetically(foldersWithRequests, 'name');
        
        setCollections(prevCollections => {
          const updated = prevCollections.map(collection => {
            if (collection.id === collectionId) {
              return { ...collection, folders: sortedFolders };
            }
            return collection;
          });
          return updated;
        });
        
        const folderRequestsMap = {};
        details.folders.forEach(folder => {
          if (folder.requests && folder.requests.length > 0) {
            const sortedRequests = sortAlphabetically(folder.requests, 'name');
            folderRequestsMap[folder.id] = sortedRequests;
          }
        });
        
        if (Object.keys(folderRequestsMap).length > 0) {
          setFolderRequests(prev => ({ ...prev, ...folderRequestsMap }));
        }
      }
      
    } catch (error) {
      console.error(`Error loading collection details:`, error);
    }
  }, [authToken]);

  // Fetch folder requests
  const fetchFolderRequests = useCallback(async (collectionId, folderId) => {
    if (!authToken || !collectionId || !folderId) return [];

    if (folderRequests[folderId] && folderRequests[folderId].length > 0) {
      return folderRequests[folderId];
    }

    try {
      const response = await getFolderRequestsFromCodebase(authToken, collectionId, folderId);
      const handledResponse = handleCodebaseResponse(response);
      const folderDetails = extractFolderRequests(handledResponse);
      
      if (folderDetails) {
        const requests = (folderDetails.requests || []).map(req => ({
          id: req.id || req.requestId,
          name: req.name || req.requestName,
          method: req.method,
          url: req.url,
          description: req.description,
          protocolType: req.protocolType || 'rest',
          headers: req.headers || [],
          body: req.body,
          responseExample: req.responseExample,
          lastModified: req.lastModified,
          tags: req.tags || []
        }));
        const sortedRequests = sortAlphabetically(requests, 'name');
        
        setFolderRequests(prev => ({ ...prev, [folderId]: sortedRequests }));
        return sortedRequests;
      }
      return [];
      
    } catch (error) {
      console.error(`Error loading folder requests:`, error);
      return [];
    }
  }, [authToken, folderRequests]);

  // Fetch request details
  const fetchRequestDetails = useCallback(async (collectionId, requestId) => {
    if (!authToken || !collectionId || !requestId) return;

    setIsLoading(prev => ({ ...prev, requestDetails: true }));
    
    try {
      const response = await getRequestDetailsFromCodebase(authToken, collectionId, requestId);
      const handledResponse = handleCodebaseResponse(response);
      const details = extractCodebaseRequestDetails(handledResponse);
      
      if (details) {
        const requestWithDetails = {
          id: details.id,
          name: details.name,
          method: details.method,
          url: details.url,
          description: details.description,
          protocolType: details.protocolType || 'rest',
          headers: details.headers || [],
          body: details.body || null,
          responseExample: details.responseExample || null,
          queryParameters: details.queryParameters || [],
          pathParameters: details.pathParameters || [],
          tags: details.tags || [],
          lastModified: details.lastModified
        };
        
        setSelectedRequestDetails(requestWithDetails);
        setSelectedRequest(requestWithDetails);
      }
      
    } catch (error) {
      console.error('Error loading request details:', error);
      showToast(`Failed to load request details: ${error.message}`, 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, requestDetails: false }));
    }
  }, [authToken]);

  // Handle select request
  const handleSelectRequest = async (request, collection) => {
    setSelectedRequest(request);
    setSelectedCollection(collection);
    await fetchRequestDetails(collection.id, request.id);
    showToast(`Viewing code for ${request.name}`, 'info');
  };

  // Toggle collection
  const toggleCollection = async (collectionId) => {
    const isExpanding = !expandedCollections.includes(collectionId);
    
    setExpandedCollections(prev =>
      prev.includes(collectionId) ? prev.filter(id => id !== collectionId) : [...prev, collectionId]
    );
    
    if (isExpanding) {
      await fetchCollectionDetails(collectionId);
    }
  };

  // Toggle folder
  const toggleFolder = async (folderId) => {
    if (isFirstLoad.current) return;
    
    const isExpanding = !expandedFolders.includes(folderId);
    
    setExpandedFolders(prev =>
      prev.includes(folderId) ? prev.filter(id => id !== folderId) : [...prev, folderId]
    );
    
    if (isExpanding && selectedCollection) {
      await fetchFolderRequests(selectedCollection.id, folderId);
    }
  };

  // Get folder requests
  const getFolderRequests = (folderId) => {
    return folderRequests[folderId] || [];
  };

  // Filter collections based on search
  const filteredCollections = collections.filter(collection => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      collection.name?.toLowerCase().includes(query) ||
      collection.description?.toLowerCase().includes(query)
    );
  });

  // Initialize data
  useEffect(() => {
    if (authToken) {
      const extractedUserId = extractUserIdFromToken(authToken);
      setUserId(extractedUserId);
      
      withGlobalLoading(async () => {
        await fetchCollectionsList();
      });
    }
  }, [authToken, fetchCollectionsList]);

  // Auto-expand first folder and select first endpoint
  useEffect(() => {
    if (!isFirstLoad.current || autoExpandTriggered.current) return;
    if (!selectedCollection) return;
    if (!selectedCollection.folders || selectedCollection.folders.length === 0) return;
    
    autoExpandTriggered.current = true;
    
    const autoExpandAndSelect = async () => {
      const firstFolder = selectedCollection.folders[0];
      if (!firstFolder) return;
      
      setExpandedFolders([firstFolder.id]);
      
      const requests = await fetchFolderRequests(selectedCollection.id, firstFolder.id);
      
      if (requests && requests.length > 0 && !selectedRequest) {
        const firstRequest = requests[0];
        setSelectedRequest(firstRequest);
        await fetchRequestDetails(selectedCollection.id, firstRequest.id);
      }
      
      isFirstLoad.current = false;
    };
    
    autoExpandAndSelect();
  }, [selectedCollection, selectedCollection?.folders, selectedRequest, fetchFolderRequests, fetchRequestDetails]);

  // Render main content
  const renderMainContent = () => {
    const currentCode = generateCodeExample();
    const currentLanguage = availableLanguages.find(lang => lang.id === selectedLanguage);
    
    return (
    <div className="flex-1 overflow-auto p-8">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          {selectedRequest ? (
            <>
              <h1 className="text-xl font-semibold mb-4 uppercase" style={{ color: colors.text }}>
                {selectedRequest.name}
              </h1>
              
              <div className="flex flex-wrap items-center gap-4 text-xs mb-4 mt-4">
                {selectedCollection && (
                  <div style={{ color: colors.textTertiary }}>
                    <Folder size={12} className="inline mr-1" style={{ color: colors.textTertiary }} />
                    {selectedCollection.name}
                  </div>
                )}
                {selectedRequest.tags && selectedRequest.tags.length > 0 && (
                  <div className="flex items-center gap-2">
                    {selectedRequest.tags.map(tag => (
                      <span key={tag} className="text-xs px-2 py-1 rounded" style={{ 
                        backgroundColor: colors.hover,
                        color: colors.textSecondary
                      }}>
                        {tag}
                      </span>
                    ))}
                  </div>
                )}
                {selectedRequest.lastModified && (
                  <div style={{ color: colors.textTertiary }}>
                    <Clock size={12} className="inline mr-1" style={{ color: colors.textTertiary }} />
                    Last updated: {selectedRequest.lastModified}
                  </div>
                )}
              </div>

              {selectedRequest.description && (
                <div className="p-4 rounded-lg" style={{ backgroundColor: colors.codeBg, border: `1px solid ${colors.border}` }}>
                  <div className="flex items-center gap-3 mb-1">
                    <ProtocolBadge protocol={selectedRequest.protocolType} size="md" colors={colors} />
                    {selectedRequest.deprecated && (
                      <span className="text-xs px-2 py-1 rounded-full flex items-center gap-1" style={{ 
                        backgroundColor: `${colors.error}20`,
                        color: colors.error
                      }}>
                        <AlertCircle size={12} /> Deprecated
                      </span>
                    )}
                    {selectedRequest.requiresAuthentication && (
                      <span className="text-xs px-2 py-1 rounded-full flex items-center gap-1" style={{ 
                        backgroundColor: `${colors.warning}20`,
                        color: colors.warning
                      }}>
                        <Lock size={12} /> Requires Auth
                      </span>
                    )}
                    <span className="text-xs px-2 py-1 rounded-full flex items-center gap-1" style={{ 
                      color: colors.textSecondary
                    }}>
                      <Info size={12} /> {selectedRequest.description}
                    </span>
                  </div>
                </div>
              )}
              
            </>
          ) : (
            <div className="text-center py-12">
              <Code size={48} className="mx-auto mb-4 opacity-50" style={{ color: colors.textSecondary }} />
              <h2 className="text-2xl font-semibold mb-4" style={{ color: colors.text }}>Select an API Endpoint</h2>
              <p className="text-base mb-6" style={{ color: colors.textSecondary }}>
                Choose an endpoint from the left sidebar to view its implementation
              </p>
            </div>
          )}
        </div>

          {selectedRequest && (
  <>
    {/* Language & Framework Selection - Grid Layout */}
    <div className="mb-8 p-6 rounded-xl border hover-lift" style={{ 
      backgroundColor: colors.card,
      borderColor: colors.border
    }}>
      <h2 className="text-lg font-semibold mb-4" style={{ color: colors.text }}>Select Language</h2>
      <div className="grid grid-cols-2 md:grid-cols-6 gap-3">
        {availableLanguages.map(lang => (
          <button
            key={lang.id}
            onClick={() => setSelectedLanguage(lang.id)}
            className={`p-4 rounded-xl text-sm text-center hover-lift transition-all ${
              selectedLanguage === lang.id ? 'ring-2 ring-offset-1' : ''
            }`}
            style={{ 
              backgroundColor: selectedLanguage === lang.id ? colors.selected : colors.hover,
              border: `1px solid ${selectedLanguage === lang.id ? colors.primary : colors.border}`,
              color: colors.text,
              boxShadow: selectedLanguage === lang.id ? `0 0 0 2px ${colors.primary}40` : 'none'
            }}
          >
            <div className="flex flex-col items-center">
              {lang.icon}
              <span className="mt-2 font-medium">{lang.name}</span>
              <span className="text-xs mt-1" style={{ color: colors.textSecondary }}>{lang.framework}</span>
              {selectedLanguage === lang.id && (
                <Check size={16} className="mt-2" style={{ color: colors.primary }} />
              )}
            </div>
          </button>
        ))}
      </div>
    </div>

    {/* Code Display */}
    <div className="border rounded-xl overflow-hidden hover-lift" style={{ 
      borderColor: colors.border,
      backgroundColor: colors.card
    }}>
      <div className="px-4 py-3 flex items-center justify-between" style={{ 
        borderBottom: `1px solid ${colors.border}`,
        backgroundColor: colors.tableHeader
      }}>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            {currentLanguage?.icon}
            <span className="font-medium" style={{ color: colors.text }}>
              {currentLanguage?.name || 'cURL'} Example
            </span>
            <span className="text-xs px-2 py-0.5 rounded" style={{ 
              backgroundColor: `${currentLanguage?.color || '#0f172a'}20`,
              color: currentLanguage?.color || '#0f172a'
            }}>
              {currentLanguage?.framework || 'Command Line'}
            </span>
          </div>
          <ProtocolBadge protocol={selectedRequest.protocolType} size="sm" colors={colors} />
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => copyToClipboard(currentCode)}
            className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            <Copy size={12} />
            Copy
          </button>
          {/* Regenerate Button */}
          <button
            onClick={async () => {
              if (!selectedCollection || !selectedRequest) {
                showToast('Please select a request first', 'warning');
                return;
              }
              
              setIsGeneratingCode(true);
              try {
                // Regenerate the code example by refreshing the request details
                await fetchRequestDetails(selectedCollection.id, selectedRequest.id);
                showToast('Code example regenerated!', 'success');
              } catch (error) {
                console.error('Error regenerating code:', error);
                showToast('Failed to regenerate code example', 'error');
              } finally {
                setIsGeneratingCode(false);
              }
            }}
            disabled={isGeneratingCode}
            className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            {isGeneratingCode ? (
              <RefreshCw size={12} className="animate-spin" />
            ) : (
              <RefreshCw size={12} />
            )}
            Regenerate
          </button>
        </div>
      </div>
      <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
        {isLoading.requestDetails || isGeneratingCode ? (
          <div className="text-center py-8">
            <RefreshCw size={16} className="animate-spin mx-auto mb-2" style={{ color: colors.textSecondary }} />
            <p className="text-sm" style={{ color: colors.textSecondary }}>
              {isGeneratingCode ? 'Generating code example...' : 'Loading code example...'}
            </p>
          </div>
        ) : (
          <SyntaxHighlighter 
            language={selectedLanguage === 'curl' ? 'bash' : selectedLanguage}
            code={currentCode}
          />
        )}
      </div>
    </div>
  </>
)}
        </div>
      </div>
    );
  };

  return (
    <div className="flex flex-col h-screen overflow-hidden" style={{ 
      backgroundColor: colors.bg,
      color: colors.text,
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
      fontSize: '13px'
    }}>
      <style>{`
        @keyframes fadeInUp {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
        
        .animate-fade-in-up {
          animation: fadeInUp 0.2s ease-out;
        }
        
        .animate-spin {
          animation: spin 1s linear infinite;
        }
        
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        ::-webkit-scrollbar {
          width: 8px;
          height: 8px;
        }
        
        ::-webkit-scrollbar-track {
          background: ${colors.border};
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb {
          background: ${colors.textTertiary};
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb:hover {
          background: ${colors.textSecondary};
        }
      `}</style>

      {/* Loading Overlay */}
      <LoadingOverlay isLoading={globalLoading || isLoading.initialLoad || isLoading.collections} colors={colors} />

      {/* TOP NAVIGATION */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-1 -ml-3 text-nowrap">
            <span className="px-3 py-1.5 text-sm font-medium rounded transition-colors uppercase" style={{ color: colors.text }}>
              API Code Base
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Code Panel Toggle */}
          <button onClick={() => setShowCodePanel(!showCodePanel)} 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: showCodePanel ? colors.selected : colors.hover }}>
            <Code size={14} style={{ color: showCodePanel ? colors.primary : colors.textSecondary }} />
          </button>

          {/* Refresh Button */}
          <button 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            onClick={async () => {
              await withGlobalLoading(async () => {
                await fetchCollectionsList();
              });
              showToast('Collections refreshed', 'success');
            }}
            style={{ backgroundColor: colors.hover }}>
            <RefreshCw size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar - Collections */}
        <div className="w-80 border-r flex flex-col" style={{ borderColor: colors.border }}>
          <div className="p-4 border-b" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Collections</h3>
              <div className="flex gap-1">
                <button 
                  className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={async () => {
                    await withGlobalLoading(async () => {
                      await fetchCollectionsList();
                    });
                    showToast('Collections refreshed', 'success');
                  }}
                  disabled={isLoading.collections}
                  style={{ backgroundColor: colors.hover }}
                >
                  <RefreshCw 
                    size={12} 
                    style={{ 
                      color: colors.textSecondary,
                      animation: isLoading.collections ? 'spin 1s linear infinite' : 'none'
                    }} 
                  />
                </button>
                
                <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={() => setShowImportModal(true)}
                  style={{ backgroundColor: colors.hover }}>
                  <Plus size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            </div>
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
              <input 
                type="text" 
                placeholder="Search collections..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-8 pr-3 py-2 rounded text-sm focus:outline-none hover-lift"
                style={{ 
                  backgroundColor: colors.inputBg, 
                  border: `1px solid ${colors.border}`, 
                  color: colors.text 
                }} 
              />
              {searchQuery && (
                <div className="absolute right-2 top-1/2 transform -translate-y-1/2">
                  <button onClick={() => setSearchQuery('')} className="p-0.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ backgroundColor: colors.hover }}>
                    <X size={12} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
              )}
            </div>
          </div>

          <div className="flex-1 overflow-auto p-2">
            {isLoading.collections ? (
              <div className="text-center py-8" style={{ color: colors.textSecondary }}>
                <RefreshCw size={16} className="animate-spin mx-auto mb-2" />
                <p className="text-sm">Loading collections...</p>
              </div>
            ) : (
              (() => {
                const collectionsWithEndpoints = filteredCollections.filter(collection => hasEndpoints(collection));
                
                if (collectionsWithEndpoints.length === 0 && !isLoading.initialLoad) {
                  return (
                    <div className="text-center p-4" style={{ color: colors.textSecondary }}>
                      <DatabaseIcon size={20} className="mx-auto mb-2 opacity-50" />
                      <p className="text-sm">No collections with endpoints found</p>
                      <button className="mt-4 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift"
                        onClick={async () => {
                          await withGlobalLoading(async () => {
                            await fetchCollectionsList();
                          });
                        }}
                        style={{ backgroundColor: colors.hover, color: colors.text }}>
                        Load Collections
                      </button>
                    </div>
                  );
                }
                
                return (
                  <>
                    {collectionsWithEndpoints.map(collection => {
                      const foldersWithEndpoints = (collection.folders || []).filter(folder => hasEndpoints(folder));
                      const totalEndpoints = foldersWithEndpoints.reduce((sum, folder) => 
                        sum + (folder.requestCount || folder.requests?.length || 0), 0);
                      
                      if (foldersWithEndpoints.length === 0 && totalEndpoints === 0) return null;
                      
                      return (
                        <div key={collection.id} className="mb-3">
                          <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer hover-lift"
                            onClick={() => toggleCollection(collection.id)}
                            style={{ backgroundColor: colors.hover }}>
                            {expandedCollections.includes(collection.id) ? (
                              <ChevronDown size={12} style={{ color: colors.textSecondary }} />
                            ) : (
                              <ChevronRight size={12} style={{ color: colors.textSecondary }} />
                            )}
                            <button onClick={(e) => {
                              e.stopPropagation();
                              const newCollections = collections.map(c => 
                                c.id === collection.id ? { ...c, isFavorite: !c.isFavorite } : c
                              );
                              setCollections(newCollections);
                              showToast(collection.isFavorite ? 'Removed from favorites' : 'Added to favorites', 'success');
                            }}>
                              {collection.isFavorite ? (
                                <Star size={12} fill="#FFB300" style={{ color: '#FFB300' }} />
                              ) : (
                                <Star size={12} style={{ color: colors.textSecondary }} />
                              )}
                            </button>
                            
                            <span className="text-sm font-medium flex-1 truncate" style={{ color: colors.text }}>
                              {collection.name}
                            </span>
                            
                            {totalEndpoints > 0 && (
                              <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                                backgroundColor: colors.primaryDark,
                                color: 'white'
                              }}>
                                {totalEndpoints}
                              </span>
                            )}
                          </div>

                          {expandedCollections.includes(collection.id) && foldersWithEndpoints.length > 0 && (
                            <>
                              {foldersWithEndpoints.map(folder => {
                                const folderRequestCount = folder.requestCount || folder.requests?.length || 0;
                                if (folderRequestCount === 0) return null;
                                
                                return (
                                  <div key={folder.id} className="ml-4 mb-2">
                                    <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer hover-lift"
                                      onClick={() => toggleFolder(folder.id)}
                                      style={{ backgroundColor: colors.hover }}>
                                      {expandedFolders.includes(folder.id) ? (
                                        <ChevronDown size={11} style={{ color: colors.textSecondary }} />
                                      ) : (
                                        <ChevronRight size={11} style={{ color: colors.textSecondary }} />
                                      )}
                                      <FolderOpen size={11} style={{ color: colors.textSecondary }} />
                                      
                                      <span className="text-sm flex-1 truncate" style={{ color: colors.text }}>
                                        {folder.name}
                                      </span>
                                      
                                      {folderRequestCount > 0 && (
                                        <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                                          backgroundColor: colors.primaryDark,
                                          color: 'white'
                                        }}>
                                          {folderRequestCount}
                                        </span>
                                      )}

                                      {folderRequestCount === 0 && isLoading.folderRequests[folder.id] && (
                                        <RefreshCw size={10} className="animate-spin" style={{ color: colors.textTertiary }} />
                                      )}
                                    </div>

                                    {expandedFolders.includes(folder.id) && (
                                      <div className="ml-6">
                                        {isLoading.folderRequests[folder.id] ? (
                                          <div className="py-2 text-center">
                                            <RefreshCw size={12} className="animate-spin mx-auto mb-1" style={{ color: colors.textSecondary }} />
                                            <p className="text-xs" style={{ color: colors.textTertiary }}>Loading endpoints...</p>
                                          </div>
                                        ) : (
                                          <>
                                            {getFolderRequests(folder.id).length > 0 ? (
                                              getFolderRequests(folder.id).map(request => {
                                                const requestId = request.id || request.requestId;
                                                const requestName = request.name || request.requestName;
                                                const requestMethod = request.method;
                                                const protocolType = request.protocolType;
                                                
                                                if (!requestId || !requestName) return null;
                                                
                                                return (
                                                  <div key={requestId} className="flex items-center gap-2 mb-1.5 group">
                                                    <button
                                                      onClick={() => handleSelectRequest(request, collection)}
                                                      className="flex items-center gap-2 text-sm text-left transition-colors flex-1 px-2 py-1.5 rounded hover:bg-opacity-50 hover-lift"
                                                      style={{ 
                                                        color: selectedRequest?.id === requestId ? colors.primary : colors.text,
                                                        backgroundColor: selectedRequest?.id === requestId ? colors.selected : 'transparent'
                                                      }}>
                                                      {requestMethod && (
                                                        <div className="w-2 h-2 rounded-full flex-shrink-0" style={{ 
                                                          backgroundColor: getMethodColor(requestMethod)
                                                        }} />
                                                      )}
                                                      
                                                      {protocolType && protocolType !== 'rest' && (
                                                        <span className="text-xs px-1 py-0.5 rounded" style={{ 
                                                          backgroundColor: protocolType === 'soap' ? `${colors.info}20` : `${colors.success}20`,
                                                          color: protocolType === 'soap' ? colors.info : colors.success
                                                        }}>
                                                          {protocolType === 'soap' ? 'SOAP' : 'GQL'}
                                                        </span>
                                                      )}
                                                      
                                                      <span className="truncate">{requestName}</span>
                                                    </button>
                                                  </div>
                                                );
                                              })
                                            ) : (
                                              <div className="py-2 text-center">
                                                <p className="text-xs" style={{ color: colors.textTertiary }}>
                                                  No endpoints in this folder
                                                </p>
                                              </div>
                                            )}
                                          </>
                                        )}
                                      </div>
                                    )}
                                  </div>
                                );
                              })}
                            </>
                          )}
                          
                          {expandedCollections.includes(collection.id) && foldersWithEndpoints.length === 0 && (
                            <div className="ml-4 py-2 text-center">
                              <p className="text-xs" style={{ color: colors.textTertiary }}>No endpoints in this collection</p>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </>
                );
              })()
            )}
            
            {filteredCollections.length === 0 && searchQuery && (
              <div className="text-center p-4" style={{ color: colors.textSecondary }}>
                <Search size={20} className="mx-auto mb-2 opacity-50" />
                <p className="text-sm">No collections found for "{searchQuery}"</p>
                <button className="mt-2 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={() => setSearchQuery('')}
                  style={{ backgroundColor: colors.hover, color: colors.text }}>
                  Clear Search
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Main Content Area */}
        {renderMainContent()}

        {/* Right Code Panel */}
        {showCodePanel && (
          <div className="w-80 border-l flex flex-col" style={{ 
            backgroundColor: colors.card,
            borderColor: colors.border
          }}>
            <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Code Example</h3>
              <button onClick={() => setShowCodePanel(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}>
                <X size={14} style={{ color: colors.textSecondary }} />
              </button>
            </div>

            <div className="px-4 py-3 border-b" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Language</div>
              <div className="relative">
                <button
                  onClick={() => setShowLanguageDropdown(!showLanguageDropdown)}
                  className="w-full px-3 py-2 rounded text-sm font-medium flex items-center justify-between hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover, color: colors.text }}
                >
                  <div className="flex items-center gap-2">
                    {availableLanguages.find(l => l.id === selectedLanguage)?.icon}
                    <span>{availableLanguages.find(l => l.id === selectedLanguage)?.name || 'cURL'}</span>
                  </div>
                  <ChevronDown size={14} style={{ color: colors.textSecondary }} />
                </button>

                {showLanguageDropdown && (
                  <div className="absolute left-0 right-0 top-full mt-1 py-2 rounded shadow-lg z-50 border"
                    style={{ 
                      backgroundColor: colors.bg,
                      borderColor: colors.border
                    }}>
                    {availableLanguages.map(lang => (
                      <button
                        key={lang.id}
                        onClick={() => {
                          setSelectedLanguage(lang.id);
                          setShowLanguageDropdown(false);
                        }}
                        className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors"
                        style={{ 
                          backgroundColor: selectedLanguage === lang.id ? colors.selected : 'transparent',
                          color: selectedLanguage === lang.id ? colors.primary : colors.text
                        }}
                      >
                        {lang.icon}
                        {lang.name}
                        {selectedLanguage === lang.id && <Check size={14} className="ml-auto" />}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div className="flex-1 overflow-auto">
              <div className="p-4 border-b flex items-center justify-between" style={{ borderColor: colors.border }}>
                <div className="flex items-center gap-2">
                  <FileCode size={12} style={{ color: colors.textSecondary }} />
                  <span className="text-sm font-medium" style={{ color: colors.text }}>Example</span>
                </div>
                <button 
                  onClick={() => {
                    const code = generateCodeExample();
                    copyToClipboard(code);
                  }}
                  className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors flex items-center gap-1 hover-lift"
                  style={{ backgroundColor: colors.hover, color: colors.text }}
                >
                  <Copy size={10} />
                  Copy
                </button>
              </div>
              
              <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                {isLoading.requestDetails ? (
                  <div className="text-center py-8">
                    <RefreshCw size={16} className="animate-spin mx-auto mb-2" style={{ color: colors.textSecondary }} />
                    <p className="text-sm" style={{ color: colors.textSecondary }}>Loading code...</p>
                  </div>
                ) : (
                  <SyntaxHighlighter 
                    language={selectedLanguage === 'curl' ? 'bash' : selectedLanguage}
                    code={generateCodeExample()}
                  />
                )}
              </div>
            </div>

            <div className="p-4 border-t" style={{ borderColor: colors.border }}>
              <button 
                className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center justify-center gap-2 hover-lift"
                onClick={() => {
                  const code = generateCodeExample();
                  copyToClipboard(code);
                }}
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                <Copy size={12} />
                Copy Code
              </button>
            </div>
          </div>
        )}
      </div>

      {/* TOAST */}
      {toast && (
        <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-50 animate-fade-in-up"
          style={{ 
            backgroundColor: toast.type === 'error' ? colors.error : 
                          toast.type === 'success' ? colors.success : 
                          toast.type === 'warning' ? colors.warning : 
                          colors.info,
            color: 'white'
          }}>
          {toast.message}
        </div>
      )}
    </div>
  );
};

export default CodeBase;