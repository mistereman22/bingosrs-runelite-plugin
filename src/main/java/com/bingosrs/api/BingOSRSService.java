package com.bingosrs.api;

import com.bingosrs.BingOSRSConfig;
import com.bingosrs.api.message.AuthResponse;
import com.bingosrs.api.model.Team;
import com.bingosrs.api.message.AuthRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import okhttp3.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
public class BingOSRSService {
    public static final String HOST = "api.bingosrs.com";

    @Inject
    private OkHttpClient client;

    @Inject
    private Gson gson;

    @Inject
    private BingOSRSConfig config;


    // Default to true so this pulls on first game tick
    private boolean shouldFetchAuth = true;
    private String accessToken;

    private String request(Request request) throws Exception {
        String json = null;
        Call call = client.newCall(request);
        Response response = null;

        try {
            response = call.execute();
            if (!response.isSuccessful()) {
                throw new IOException("Request failed with code: " + response.code() + " message: " + response.message());
            }
            ResponseBody body = response.body();
            if (body != null) {
                json = body.string();
            }
            return json;

        } catch (IOException e) {
            throw e;
        }
        finally {
            if(response != null) {
                response.close();
            }
        }
    }

    public void onGameTick(GameTick gameTick) {
        if (this.shouldFetchAuth) {
            this.shouldFetchAuth = false;
            if (config.bingoId().isBlank() || config.playerToken().isBlank()) {
                return;
            }
            this.fetchAuthTokenAsync();
        }
    }

    public void triggerAuth() {
        this.shouldFetchAuth = true;
    }

    private CompletableFuture<Void> fetchAuthTokenAsync() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        HttpUrl url = new HttpUrl.Builder().scheme("https").host(HOST)
                .addPathSegment("auth").addPathSegment("login").addPathSegment("player").build();

        AuthRequest authRequest = new AuthRequest(config.bingoId(), config.playerToken());
        String json = gson.toJson(authRequest);

        RequestBody body = RequestBody.create(MediaType.get("application/json"), json);
        Request request = new Request.Builder().url(url).post(body).build();

        CompletableFuture.supplyAsync(() -> {
            try {
                String syncResponseJSON = request(request);
                AuthResponse authResponse = gson.fromJson(syncResponseJSON, AuthResponse.class);
                this.accessToken = authResponse.accessToken;
                log.debug("Successfully authenticated for bingo");
                future.complete(null);
            } catch (Exception e) {
                accessToken = null;
                log.debug("Authentication failed for bingo");
                future.completeExceptionally(e);
            }
            return null;
        });

        return future;
    }

    public CompletableFuture<Team[]> fetchTeamsAsync() {
        CompletableFuture<Team[]> future = new CompletableFuture<>();

        HttpUrl url = new HttpUrl.Builder().scheme("https").host(HOST)
                .addPathSegment("bingo").addPathSegment(config.bingoId()).addPathSegment("teams").build();

        Request request = new Request.Builder().url(url).build();

        CompletableFuture.supplyAsync(() -> {
            try {
                String syncResponseJSON = request(request);

                Type teamListType = new TypeToken<Team[]>(){}.getType();
                Team[] teams = gson.fromJson(syncResponseJSON, teamListType);

                future.complete(teams);
            } catch (Exception e) {
                log.debug("Error while fetching teams: {}", e.getMessage());
                future.completeExceptionally(e);
            }
            return null; // supplyAsync requires a return value, but it won't be used here
        });

        return future;
    }

    public CompletableFuture<String> submitDropAsync(String bingoId, byte[] screenshotBytes, String player, Integer itemId, Integer npcId) {
        return submitDropAttempt(bingoId, screenshotBytes, player, itemId, npcId, false); // initial attempt
    }

    private CompletableFuture<String> submitDropAttempt(String bingoId, byte[] screenshotBytes, String player, int itemId, int npcId, boolean isRetry) {
        CompletableFuture<String> future = new CompletableFuture<>();

        HttpUrl url = new HttpUrl.Builder().scheme("https").host(HOST)
                .addPathSegment("bingo").addPathSegment(bingoId).addPathSegment("drop").build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("player", player)
                .addFormDataPart("boss", Integer.toString(npcId))
                .addFormDataPart("item", Integer.toString(itemId))
                .addFormDataPart("screenshot", "screenshot.png",
                        RequestBody.create(MediaType.parse("image/png"), screenshotBytes))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        CompletableFuture.supplyAsync(() -> {
            try {
                String responseString = request(request);
                future.complete(responseString);
            } catch (Exception e) {
                if (e.getMessage().contains("401") && !isRetry) {
                    log.debug("Auth error submitting drop to bingo: {}. Reauthenticating...", bingoId);
                    // If auth error, force re-auth and then retry once
                    fetchAuthTokenAsync()
                            .thenRun(() -> {
                                submitDropAttempt(bingoId, screenshotBytes, player, itemId, npcId, true)
                                        .whenComplete((result, throwable) -> {
                                            if (throwable != null) {
                                                future.completeExceptionally(throwable);
                                            } else {
                                                future.complete(result);
                                            }
                                        });
                            })
                            .exceptionally(throwable -> {
                                future.completeExceptionally(throwable);
                                return null;
                            });

                } else {
                    log.debug("Error submitting bingo drop: {}", e.getMessage());
                    future.completeExceptionally(e);
                }
            }
            return null;
        });

        return future;
    }
}
