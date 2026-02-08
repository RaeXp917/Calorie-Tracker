package com.example.calorie_tracker.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calorie_tracker.R
import com.example.calorie_tracker.domain.model.Gender
import com.example.calorie_tracker.domain.model.Goal
import com.example.calorie_tracker.presentation.components.FilledButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val step = viewModel.currentStep
    BackHandler(enabled = step > 0) {
        viewModel.onBackClick()
    }
    Scaffold(
        topBar = {
            if (step > 0) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onBackClick() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.button_back)
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            FilledButton(
                text = if (step == 6) stringResource(R.string.button_finish) else stringResource(R.string.button_next),
                onClick = {
                    if (step == 6) onFinish() else viewModel.onNextClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = if (step == 6) Icons.Default.Check else Icons.Default.ArrowForward
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (step) {
                0 -> LanguageStep(viewModel)
                1 -> GenderStep(viewModel)
                2 -> AgeStep(viewModel)
                3 -> HeightStep(viewModel)
                4 -> WeightStep(viewModel)
                5 -> GoalStep(viewModel)
                6 -> ResultStep(viewModel)
            }
        }
    }
}

// --- Step 0: Language Selection ---
@Composable
fun LanguageStep(viewModel: OnboardingViewModel) {
    val context = LocalContext.current

    Text(
        text = stringResource(R.string.onboarding_language_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(24.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledButton(
            text = stringResource(R.string.onboarding_language_english),
            onClick = {
                viewModel.onLanguageSelected("Eng", context)
            },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (viewModel.selectedLanguage == "Eng")
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (viewModel.selectedLanguage == "Eng")
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Default.Language
        )

        FilledButton(
            text = stringResource(R.string.onboarding_language_greek),
            onClick = {
                viewModel.onLanguageSelected("Gr", context)
            },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (viewModel.selectedLanguage == "Gr")
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (viewModel.selectedLanguage == "Gr")
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Default.Language
        )
    }
}

// --- Step 1: Gender ---
@Composable
fun GenderStep(viewModel: OnboardingViewModel) {
    Text(
        text = stringResource(R.string.onboarding_gender_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledButton(
            text = stringResource(R.string.onboarding_gender_male),
            onClick = { viewModel.onGenderSelected(Gender.MALE) },
            modifier = Modifier.weight(1f),
            backgroundColor = if (viewModel.gender == Gender.MALE)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (viewModel.gender == Gender.MALE)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledButton(
            text = stringResource(R.string.onboarding_gender_female),
            onClick = { viewModel.onGenderSelected(Gender.FEMALE) },
            modifier = Modifier.weight(1f),
            backgroundColor = if (viewModel.gender == Gender.FEMALE)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (viewModel.gender == Gender.FEMALE)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(
            R.string.onboarding_gender_selected,
            viewModel.gender?.name ?: stringResource(R.string.onboarding_gender_none)
        ),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun NumberWheelPicker(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    visibleItemsCount: Int = 5
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedValue - range.first
    )

    val itemHeight = 48.dp
    val centerIndex = visibleItemsCount / 2

    LaunchedEffect(listState.firstVisibleItemIndex) {
        val value = range.first + listState.firstVisibleItemIndex + centerIndex
        if (value in range) onValueChange(value)
    }

    Box(
        modifier = Modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(range.count()) { index ->
                val value = range.first + index
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.height(itemHeight)
                )
            }
        }
    }
}

@Composable
fun AgeStep(viewModel: OnboardingViewModel) {
    Text(
        text = stringResource(R.string.onboarding_age_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(24.dp))

    NumberWheelPicker(
        range = 15..100,
        selectedValue = viewModel.age,
        onValueChange = { viewModel.age = it }
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_age_years, viewModel.age),
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
fun HeightStep(viewModel: OnboardingViewModel) {
    val heightRange = when (viewModel.gender) {
        Gender.MALE -> 155..210
        Gender.FEMALE -> 145..190
        null -> 140..230
    }

    Text(
        text = stringResource(R.string.onboarding_height_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(24.dp))

    NumberWheelPicker(
        range = heightRange,
        selectedValue = viewModel.height.coerceIn(heightRange),
        onValueChange = { viewModel.height = it }
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_height_value, viewModel.height),
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
fun WeightStep(viewModel: OnboardingViewModel) {
    val weightRange = when (viewModel.gender) {
        Gender.MALE -> 55..200
        Gender.FEMALE -> 45..160
        null -> 50..200
    }
    Text(
        text = stringResource(R.string.onboarding_weight_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(24.dp))
    NumberWheelPicker(
        range = weightRange,
        selectedValue = viewModel.weight.toInt().coerceIn(weightRange),
        onValueChange = { viewModel.weight = it.toDouble() }
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_weight_value, viewModel.weight.toInt()),
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
fun GoalStep(viewModel: OnboardingViewModel) {
    Text(
        text = stringResource(R.string.onboarding_goal_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(24.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledButton(
            text = stringResource(R.string.onboarding_goal_lose),
            onClick = { viewModel.goal = Goal.LOSE_WEIGHT },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (viewModel.goal == Goal.LOSE_WEIGHT)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (viewModel.goal == Goal.LOSE_WEIGHT)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilledButton(
            text = stringResource(R.string.onboarding_goal_maintain),
            onClick = { viewModel.goal = Goal.MAINTAIN },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (viewModel.goal == Goal.MAINTAIN)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (viewModel.goal == Goal.MAINTAIN)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilledButton(
            text = stringResource(R.string.onboarding_goal_gain),
            onClick = { viewModel.goal = Goal.GAIN_WEIGHT },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (viewModel.goal == Goal.GAIN_WEIGHT)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (viewModel.goal == Goal.GAIN_WEIGHT)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(
            R.string.onboarding_goal_selected,
            viewModel.goal?.name ?: stringResource(R.string.onboarding_gender_none)
        ),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ResultStep(viewModel: OnboardingViewModel) {
    val calories = viewModel.calculateDailyCalories()

    Text(
        text = stringResource(R.string.onboarding_result_title),
        style = MaterialTheme.typography.headlineLarge
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.onboarding_result_calories, calories),
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.onboarding_result_protein, (viewModel.weight * 2).toInt())
    )
}