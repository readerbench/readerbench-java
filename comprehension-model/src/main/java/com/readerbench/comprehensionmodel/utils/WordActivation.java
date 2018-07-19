/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.comprehensionmodel.utils;

/**
 *
 * @author ionutparaschiv
 */
public class WordActivation {

    private final double activationValue;
    private final boolean isActive;

    public WordActivation(double activationValue, boolean isActive) {
        this.activationValue = activationValue;
        this.isActive = isActive;
    }

    public double getActivationValue() {
        return activationValue;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return this.activationValue + "," + (this.isActive ? "X" : "");
    }
}
