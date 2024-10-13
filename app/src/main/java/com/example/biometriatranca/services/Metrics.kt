package com.example.biometriatranca

fun mean(numbers: List<Double>): Double {
    return if (numbers.isNotEmpty()) {
        numbers.sum() / numbers.size
    } else {
        0.0
    }
}

fun mode(numbers: List<Double>): List<Double> {
    if (numbers.isEmpty()) return emptyList()

    val frequencyMap = numbers.groupingBy { it }.eachCount()
    val maxFrequency = frequencyMap.values.maxOrNull() ?: return emptyList()

    return frequencyMap.filter { it.value == maxFrequency }.keys.toList()
}

fun median(numbers: List<Double>): Double {
    if (numbers.isEmpty()) return 0.0

    val sortedNumbers = numbers.sorted()
    val size = sortedNumbers.size

    return if (size % 2 == 0) {
        (sortedNumbers[size / 2 - 1] + sortedNumbers[size / 2]) / 2
    } else {
        sortedNumbers[size / 2]
    }
}

fun variance(numbers: List<Double>): Double {
    if (numbers.isEmpty()) return 0.0

    val meanValue = mean(numbers)
    return numbers.map { (it - meanValue) * (it - meanValue) }.sum() / numbers.size
}

fun standardDeviation(numbers: List<Double>): Double {
    return kotlin.math.sqrt(variance(numbers))
}

fun analyzeMessageDeliveryTimes(times: List<Long>): String {
    if (times.isEmpty()) {
        return "No data available."
    }

    val timesInSeconds = times.map { it / 1000.0 }

    val meanValue = mean(timesInSeconds)
    val modeValue = mode(timesInSeconds)
    val medianValue = median(timesInSeconds)
    val varianceValue = variance(timesInSeconds)
    val standardDeviationValue = standardDeviation(timesInSeconds)

    return """
        Mean: $meanValue seconds
        Mode: $modeValue seconds
        Median: $medianValue seconds
        Variance: $varianceValue seconds
        Standard Deviation: $standardDeviationValue seconds
    """.trimIndent()
}