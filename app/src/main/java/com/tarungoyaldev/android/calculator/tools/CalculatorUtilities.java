package com.tarungoyaldev.android.calculator.tools;

import android.util.Log;

import com.tarungoyaldev.android.calculator.MainActivity;

import java.text.DecimalFormat;

/**
 * utilities functions for calculator
 */
public class CalculatorUtilities {

    public static String convertDoubleToString(double value) {
        if (value > 1E10 || value < 1E-10 && value > 1E-50) {
            return (new DecimalFormat("0.#####E0")).format(value);
        } else if (value > 1E5){
            return (new DecimalFormat("##########0.#####")).format(value);
        } else {
            return (new DecimalFormat("##########0.##########")).format(value);
        }
    }

    public static double convertStringToDouble(String valueString) {
        try {
            if (valueString.equals("∞")) {
                return Double.POSITIVE_INFINITY;
            } else if (valueString.equals("-∞")) {
                return Double.NEGATIVE_INFINITY;
            } else {
                return Double.valueOf(valueString);
            }
        } catch (NumberFormatException e) {
            Log.e("calculatorUtilities", "Invalid Double: ", e);
            return 0;
        }
    }

    public static String applyOperationAndGetString(MainActivity.Operation operation,
                                                    double firstValue, double secondValue) {
        return convertDoubleToString(applyOperation(operation, firstValue, secondValue));
    }

    public static double asinh (double x) {
        return Math.log(x + Math.sqrt(1 + x*x));
    }

    public static double acosh (double x) {
        return Math.log(x + Math.sqrt(1 - x*x));
    }

    public static double atanh (double x) {
        return 0.5 * Math.log((x+1)/(x-1));
    }

    public static double applyOperation(MainActivity.Operation operation,
                                        double firstValue, double secondValue) {
        if (firstValue == Double.NEGATIVE_INFINITY || secondValue == Double.NEGATIVE_INFINITY) {
            return Double.NEGATIVE_INFINITY;
        } else if (firstValue == Double.POSITIVE_INFINITY || secondValue == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        }
        double result;
        switch (operation) {
            case ADDITION:
                result = firstValue + secondValue;
                break;
            case SUBTRACTION:
                result = firstValue - secondValue;
                break;
            case MULTIPLICATION:
                result = firstValue * secondValue;
                break;
            case DIVISION:
                result = firstValue / secondValue;
                break;
            case POW:
                result = Math.pow(firstValue, secondValue);
                break;
            case EE:
                result = firstValue * Math.pow(10, secondValue);
                break;
            case EQUAL:
            default:
                result = secondValue;
                break;
        }
        return result;
    }
    public static double factorial(double value) {
        boolean isNegative = value < 0;
        if (isNegative) value = 0 - value;
        Double result;
        if (value > 120) {
            result = Double.POSITIVE_INFINITY;
        } else {
            if (value % 1 == 0 && value > 0) {
                result = 1.0;
                while (value > 0) {
                    result *= value;
                    value -= 1;
                }
            } else {
                result = gamma(value + 1);
            }
        }
        return isNegative ? 0 - result : result;
    }

    public static double gamma(double value) {
        int g = 7;
        double p[] = {
                0.99999999999980993,
                676.5203681218851,
                -1259.1392167224028,
                771.32342877765313,
                -176.61502916214059,
                12.507343278686905,
                -0.13857109526572012,
                9.9843695780195716e-6,
                1.5056327351493116e-7
        };
        if (value < 0.5) {
            return Math.PI / Math.sin(value * Math.PI / gamma(1-value));
        } else {
            value--;
            double x = p[0];
            for(int i = 1; i < g + 2; i++) {
                x += p[i] / (value + i);
            }
            double t = value + g + 0.5;
            return Math.sqrt(2 * Math.PI) * Math.pow(t, (value + 0.5)) * Math.exp(-t) * x;
        }
    }
}
