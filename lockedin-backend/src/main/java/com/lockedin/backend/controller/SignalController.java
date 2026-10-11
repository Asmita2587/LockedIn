package com.lockedin.backend.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Minimal WebRTC signaling (offer/answer/ICE) over plain HTTP polling. Video itself is peer-to-peer. */
@RestController
@RequestMapping("/api/signal")
public class SignalController {

    private final Map<String, List<Map<String, Object>>> rooms = new ConcurrentHashMap<>();

    @PostMapping("/{room}")
    public void post(@PathVariable String room, @RequestBody Map<String, Object> msg) {
        List<Map<String, Object>> list = rooms.computeIfAbsent(room, k -> new ArrayList<>());
        synchronized (list) {
            list.add(msg);
        }
    }

    /** since = -1 -> just return the current cursor (used when joining). */
    @GetMapping("/{room}")
    public Map<String, Object> poll(@PathVariable String room,
                                    @RequestParam String client,
                                    @RequestParam int since) {
        List<Map<String, Object>> list = rooms.computeIfAbsent(room, k -> new ArrayList<>());
        List<Map<String, Object>> out = new ArrayList<>();
        int n;
        synchronized (list) {
            n = list.size();
            if (since >= 0) {
                for (int i = Math.min(since, n); i < n; i++) {
                    Map<String, Object> m = list.get(i);
                    Object to = m.get("to");
                    if (client.equals(m.get("from"))) continue;
                    if (to == null || client.equals(to)) out.add(m);
                }
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("msgs", out);
        res.put("next", n);
        return res;
    }
}
