package org.example.clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.config.RequestSpecConfig;

import java.io.File;

import static io.restassured.RestAssured.given;

/**
 * Client for Trees & Forestry endpoints (PRO+):
 * <ul>
 *   <li>POST /v1/trees/analyze       — count trees and assess canopy health</li>
 *   <li>GET  /v1/trees/history       — list past analyses</li>
 *   <li>GET  /v1/trees/quota         — remaining monthly quota</li>
 *   <li>POST /v1/forestry/count-trees — legacy alias for /v1/trees/analyze</li>
 * </ul>
 */
public class ForestryClient {

    private final RequestSpecification multipartSpec;
    private final RequestSpecification jsonSpec;

    public ForestryClient() {
        this.multipartSpec = RequestSpecConfig.multipartSpec();
        this.jsonSpec      = RequestSpecConfig.defaultSpec();
    }

    public ForestryClient(RequestSpecification spec) {
        this.multipartSpec = spec;
        this.jsonSpec      = spec;
    }

    /**
     * Upload a farm image and analyse tree crowns / canopy health.
     
     * @param imageFile   JPEG, PNG, or WEBP — max 20 MB
     * @param farmerId    optional farmer / plot identifier
     * @param county      optional county / region
     * @param landAcres   optional plot size in acres
     * @param location    optional human-readable farm name
     * @param notes       optional extra context for Gemini
     */
    public Response analyzeTrees(File imageFile,
                                 String farmerId,
                                 String county,
                                 Double landAcres,
                                 String location,
                                 String notes) {
        var req = given(multipartSpec)
                .multiPart("image", imageFile);

        if (farmerId  != null) req = req.multiPart("farmerId",   farmerId);
        if (county    != null) req = req.multiPart("county",     county);
        if (landAcres != null) req = req.multiPart("landAcres",  landAcres);
        if (location  != null) req = req.multiPart("location",   location);
        if (notes     != null) req = req.multiPart("notes",      notes);

        return req.post("/v1/trees/analyze");
    }

    /** List past analyses for this account. */
    public Response getTreeHistory() {
        return given(jsonSpec).get("/v1/trees/history");
    }

    /** Check remaining tree-analysis quota for the current month. */
    public Response getTreeQuota() {
        return given(jsonSpec).get("/v1/trees/quota");
    }

    /**
     * Legacy alias — identical behaviour to {@link #analyzeTrees}.
     */
    public Response countTrees(File imageFile,
                               String farmerId,
                               String county,
                               Double landAcres) {
        var req = given(multipartSpec)
                .multiPart("image", imageFile);

        if (farmerId  != null) req = req.multiPart("farmerId",  farmerId);
        if (county    != null) req = req.multiPart("county",    county);
        if (landAcres != null) req = req.multiPart("landAcres", landAcres);

        return req.post("/v1/forestry/count-trees");
    }
}
