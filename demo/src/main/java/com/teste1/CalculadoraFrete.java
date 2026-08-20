package com.teste1;

public class CalculadoraFrete {

    public double calcula(double valor, boolean premium){
        if(premium || valor >= 200){
            return 0.0;
        }
        return 20.0;
    }

}
