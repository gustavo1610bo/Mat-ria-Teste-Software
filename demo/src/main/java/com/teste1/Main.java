package com.teste1;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        CalculadoraFrete calculadoraFrete = new CalculadoraFrete();
    
        System.out.println(calculadoraFrete.calcula(20.0, false));
    
    }
}