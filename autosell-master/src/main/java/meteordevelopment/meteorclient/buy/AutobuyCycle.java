///*
// * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
// * Copyright (c) Meteor Development.
// */
//
//package meteordevelopment.meteorclient.buy;
//
//import meteordevelopment.meteorclient.MeteorClient;
//import net.minecraft.client.MinecraftClient;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//public class AutobuyCycle {
//    private AutobuySetup setup;
//    private Thread cycleThread;
//    public AutobuyCycle(AutobuySetup setup){
//        this.setup = setup;
//    }
//
//    public void runCycle(ArrayList<String> args) {
//        MeteorClient.EVENT_BUS.subscribe(this);
//
//        if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().player == null)
//            return;
//        MinecraftClient client = MinecraftClient.getInstance();
//
//        if (cycleThread != null && cycleThread.isAlive()) {
//            System.out.println("Autobuy thread is already running.");
//            return;
//        }
//
//
//        List<String> lowerArgs = setup.getArgs().stream()
//            .filter(Objects::nonNull)
//            .map(String::toLowerCase)
//            .toList();
//
//    }
//
//}
