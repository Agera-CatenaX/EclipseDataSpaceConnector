/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.catalog.atlas.metadata;

import okhttp3.*;
import org.eclipse.dataspaceconnector.catalog.atlas.dto.*;
import org.eclipse.dataspaceconnector.schema.RelationshipSchema;
import org.eclipse.dataspaceconnector.schema.SchemaAttribute;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.dataspaceconnector.catalog.atlas.dto.Functions.*;
import static org.eclipse.dataspaceconnector.catalog.atlas.metadata.HttpUtil.addBasicAuth;


public class AtlasApiImpl implements AtlasApi {
    private static final String API_PREFIX = "/api/atlas/v2";
    private static final MediaType JSON = MediaType.get("application/json");
    private final static String TYPEDEFS_API = "/types/typedefs";
    private static final String ENTITY_API = "/entity";
    private final String atlasBaseUrl;
    private final OkHttpClient httpClient;
    private final TypeManager typeManager;


    public AtlasApiImpl(String url, String username, String password, OkHttpClient client, TypeManager typeManager) {
        atlasBaseUrl = url;
        httpClient = addBasicAuth(createUnsecureClient(client), username, password);
        this.typeManager = typeManager;
    }

    @Override
    public AtlasTypesDef createClassifications(String... classificationName) {
        var defs = Arrays.stream(classificationName).map(name ->
                        createTraitTypeDef(name, Collections.emptySet()))
                .collect(Collectors.toList());

        var typedef = new AtlasTypesDef();
        typedef.setClassificationDefs(defs);


        var url = createAtlasUrl(TYPEDEFS_API);
        var rqBody = RequestBody.create(typeManager.writeValueAsString(typedef), JSON);
        var request = new Request.Builder().url(url).post(rqBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (response.isSuccessful()) {
                return typeManager.readValue(response.body().string(), AtlasTypesDef.class);
            } else {
                throw new EdcException(Objects.requireNonNull(response.body()).string());
            }
        } catch (IOException e) {
            throw new EdcException(e);
        }

    }

    @Override
    public void deleteClassification(String classificationName) {

        classificationName = Objects.requireNonNull(classificationName, "classificationName");
        var url = createAtlasUrl(TYPEDEFS_API + "?name=" + classificationName);
        var rq = new Request.Builder().url(url).build();

        try (Response response = httpClient.newCall(rq).execute()) {

            if (response.isSuccessful() && response.body() != null) {
                var body = Objects.requireNonNull(response.body()).string();
                AtlasTypesDef result = typeManager.readValue(body, AtlasTypesDef.class);
                if (result.getClassificationDefs().isEmpty()) {
                    throw new EdcException("No Classification types exist for the given names: " + classificationName);

                }
                deleteType(Collections.singletonList(result));

            }
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    @Override
    public AtlasTypesDef createCustomTypes(String typeName, Set<String> superTypeNames, List<? extends SchemaAttribute> attributes) {
        typeName = sanitize(typeName);
        var attrs = attributes.stream().map(attr -> attr.isRequired()
                ? createRequiredAttrDef(attr.getName(), attr.getType())
                : createOptionalAttrDef(attr.getName(), attr.getType())).collect(Collectors.toList());

        AtlasEntityDef atlasEntityDef = createClassTypeDef(typeName, superTypeNames);
        atlasEntityDef.setAttributeDefs(attrs);

        var typesDef = new AtlasTypesDef();
        typesDef.setEntityDefs(Collections.singletonList(atlasEntityDef));

        if (existsType(typeName)) {
            return updateTypesDef(typesDef);
        } else {
            return createTypesDef(typesDef);
        }

    }

    @Override
    public void deleteCustomType(String typeName) {

        var typesDef = getAllTypes(typeName);
        if (typesDef.getEntityDefs().isEmpty()) {
            throw new EdcException("No Custom TypeDef types exist for the given names: " + typeName);
        }
        deleteType(Collections.singletonList(typesDef));
    }

    @Override
    public void deleteEntities(List<String> entityGuids) {

        var urlBuilder = Objects.requireNonNull(HttpUrl.parse(createAtlasUrl(ENTITY_API + "/bulk"))).newBuilder();

        entityGuids.forEach(guid -> urlBuilder.addQueryParameter("guid", guid));
        var url = urlBuilder.build();

        var rq = new Request.Builder().url(url)
                .delete()
                .build();

        try (Response response = httpClient.newCall(rq).execute()) {
            if (!response.isSuccessful()) {
                throw new EdcException(response.body() != null ? Objects.requireNonNull(response.body()).string() : "deleting entities types failed");
            } else {
                var body = Objects.requireNonNull(response.body()).string();
                var emr = typeManager.readValue(body, EntityMutationResponse.class);
            }
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    @Override
    public AtlasTypesDef createRelationshipType(String name, String description, int relationshipCategory, RelationshipSchema.EndpointDefinition startDefinition, RelationshipSchema.EndpointDefinition endDefinition) {
        name = sanitize(name);
        var end1 = new AtlasRelationshipEndDef(sanitize(startDefinition.getTypeName()), sanitize(startDefinition.getName()), cardinalityFromInteger(startDefinition.getCardinality()));
        var end2 = new AtlasRelationshipEndDef(sanitize(endDefinition.getTypeName()), sanitize(endDefinition.getName()), cardinalityFromInteger(endDefinition.getCardinality()));

        var relationshipDef = createRelationshipTypeDef(name, description, "1.0", AtlasRelationshipDef.RelationshipCategory.ASSOCIATION, AtlasRelationshipDef.PropagateTags.NONE, end1, end2);

        var typesDef = new AtlasTypesDef();
        typesDef.setRelationshipDefs(Collections.singletonList(relationshipDef));

        if (existsType(name)) {
            return updateTypesDef(typesDef);
        } else {
            return createTypesDef(typesDef);
        }
    }

    @Override
    public AtlasRelationship createRelationship(String sourceEntityGuid, String targetEntityGuid, String name) {
        name = sanitize(name);
        AtlasRelationship relationship = new AtlasRelationship(name, new AtlasObjectId(sourceEntityGuid), new AtlasObjectId(targetEntityGuid));
        var url = createAtlasUrl("/relationship");

        var rq = new Request.Builder().url(url).post(RequestBody.create(typeManager.writeValueAsString(relationship), JSON)).build();

        try (var response = httpClient.newCall(rq).execute()) {
            if (!response.isSuccessful()) {
                throw new EdcException(response.body() != null ? Objects.requireNonNull(response.body()).string() : "deleting type failed");
            }

            var json = Objects.requireNonNull(response.body()).string();
            return typeManager.readValue(json, AtlasRelationship.class);
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    @Override
    public void deleteType(List<AtlasTypesDef> classificationTypes) {
        for (AtlasTypesDef type : classificationTypes) {
            var deleteUrl = createAtlasUrl(TYPEDEFS_API);
            var rq = new Request.Builder().url(deleteUrl)
                    .delete(RequestBody.create(typeManager.writeValueAsString(type), JSON)).build();

            try (Response response = httpClient.newCall(rq).execute()) {
                if (!response.isSuccessful()) {
                    throw new EdcException(response.body() != null ? Objects.requireNonNull(response.body()).string() : "deleting type failed");
                }
            } catch (IOException e) {
                throw new EdcException(e);
            }
        }
    }

    @Override
    public AtlasEntity.AtlasEntityWithExtInfo getEntityById(String id) {

        var url = createAtlasUrl(ENTITY_API + "/guid/" + id);
        var rq = new Request.Builder().url(url).get().build();

        try (Response response = httpClient.newCall(rq).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    return null;
                }
                throw new EdcException(response.body() != null ? Objects.requireNonNull(response.body()).string() : "getting entity by ID failed");
            }
            String json = Objects.requireNonNull(response.body()).string();
            return typeManager.readValue(json, AtlasEntity.AtlasEntityWithExtInfo.class);

        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    @Override
    public String createEntity(String typeName, Map<String, Object> properties) {
        AtlasEntity atlasEntity = new AtlasEntity(sanitize(typeName));

        for (String key : properties.keySet()) {
            if (key.equals("classifications")) {
                continue;
            }
            atlasEntity.setAttribute(key, properties.get(key));
        }

        List<String> classificationNames = (List<String>) properties.get("classifications");
        atlasEntity.setClassifications(toAtlasClassifications(classificationNames));
        EntityMutationResponse response = createEntity(new AtlasEntity.AtlasEntityWithExtInfo(atlasEntity));
        var guidMap = response.getGuidAssignments();
        if (guidMap.size() != 1) {
            throw new EdcException("Try to create one entity but received multiple guids back.");
        } else {
            return guidMap.entrySet().iterator().next().getValue();
        }
    }

    @Override
    public AtlasTypesDef getAllTypes(String name) {
        var url = createAtlasUrl(TYPEDEFS_API + "?name=" + name);
        var request = new Request.Builder().url(url).get().build();

        try (var response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() && response.body() != null) {
                throw new EdcException(response.body() != null ? Objects.requireNonNull(response.body()).string() : "getting all types with name " + name + " failed");
            } else {
                var body = Objects.requireNonNull(response.body()).string();

                return typeManager.readValue(body, AtlasTypesDef.class);

            }
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    @Override
    public AtlasSearchResult dslSearchWithParams(String query, int limit, int offset) {

        query = sanitize(query);
        var url = createAtlasUrl("/search/dsl");
        var httpUrl = Objects.requireNonNull(HttpUrl.parse(url), "Could not parse url " + url).newBuilder()
                .addQueryParameter("query", query)
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("offset", String.valueOf(offset))
                .build();

        var rq = new Request.Builder().url(httpUrl).get().build();


        try (var response = httpClient.newCall(rq).execute()) {
            if (!response.isSuccessful() && response.body() != null) {

                AtlasErrorCode atlasErrorCode = typeManager.readValue(response.body().string(), AtlasErrorCode.class);
                throw new AtlasQueryException(atlasErrorCode);

//                throw new EdcException(response.body() != null ? Objects.requireNonNull(response.body()).string() : "executing a DSL query failed");
            } else {
                var body = Objects.requireNonNull(response.body()).string();
                return typeManager.readValue(body, AtlasSearchResult.class);
            }
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private boolean existsType(String typeName) {
        return !getAllTypes(typeName).getEntityDefs().isEmpty();
    }

    private String sanitize(String input) {
        return input.replace(":", "_");
    }

    AtlasTypesDef createTypesDef(AtlasTypesDef atlasTypesDef) {
        var url = createAtlasUrl(TYPEDEFS_API);
        var body = RequestBody.create(typeManager.writeValueAsString(atlasTypesDef), JSON);
        var rqBuilder = new Request.Builder().url(url);
        rqBuilder.post(body);
        var rq = rqBuilder.build();

        try (var response = httpClient.newCall(rq).execute()) {
            if (!response.isSuccessful()) {
                throw new EdcException(response.body() != null ? Objects.requireNonNull(response.body()).string() : "creating custom types failed");
            } else {
                var json = Objects.requireNonNull(response.body()).string();
                return typeManager.readValue(json, AtlasTypesDef.class);
            }
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    AtlasTypesDef updateTypesDef(AtlasTypesDef atlasTypesDef) {
        var url = createAtlasUrl(TYPEDEFS_API);
        var body = RequestBody.create(typeManager.writeValueAsString(atlasTypesDef), JSON);
        var rqBuilder = new Request.Builder().url(url);
        rqBuilder.put(body);
        var rq = rqBuilder.build();

        try (var response = httpClient.newCall(rq).execute()) {
            if (!response.isSuccessful()) {
                throw new EdcException(response.body() != null ? Objects.requireNonNull(response.body()).string() : "creating custom types failed");
            } else {
                var json = Objects.requireNonNull(response.body()).string();
                return typeManager.readValue(json, AtlasTypesDef.class);
            }
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private String createAtlasUrl(String api) {
        return atlasBaseUrl + API_PREFIX + api;
    }

    private List<AtlasClassification> toAtlasClassifications(List<String> classificationNames) {
        if (classificationNames != null) {
            return classificationNames.stream().map(AtlasClassification::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private EntityMutationResponse createEntity(AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo) {
        var url = createAtlasUrl(ENTITY_API);

        var rq = new Request.Builder().url(url).post(RequestBody.create(typeManager.writeValueAsString(atlasEntityWithExtInfo), JSON)).build();

        try (Response response = httpClient.newCall(rq).execute()) {

            if (!response.isSuccessful()) {
                throw new EdcException(response.body() != null ? Objects.requireNonNull(response.body()).string() : "creating entity failed");
            }
            var json = Objects.requireNonNull(response.body()).string();
            return typeManager.readValue(json, EntityMutationResponse.class);
        } catch (IOException e) {
            throw new EdcException(e);
        }

    }

    private AtlasStructDef.AtlasAttributeDef.Cardinality cardinalityFromInteger(int cardinality) {
        return AtlasStructDef.AtlasAttributeDef.Cardinality.values()[cardinality];
    }

    /**
     * Creates an HTTP client that foregoes all SSL certificate validation. The original client is NOT modified, rather, a
     * new OkHttpClient is used.
     * <p>
     * ### THIS IS INSECURE!! DO NOT USE IN PRODUCTION CODE!!!! ###
     */
    @Deprecated
    private OkHttpClient createUnsecureClient(OkHttpClient httpClient) {
        try {
            // Create a trust manager that does not validate certificate chains
            X509TrustManager x509TrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            };
            TrustManager[] trustAllCerts = new TrustManager[]{
                    x509TrustManager
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();


            return httpClient.newBuilder()
                    .sslSocketFactory(sslSocketFactory, x509TrustManager)
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new EdcException("Error making the http client unsecure!", e);
        }

    }
}
