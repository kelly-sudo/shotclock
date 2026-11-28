package pl.shotclock.budzik.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import pl.shotclock.budzik.R
import pl.shotclock.budzik.alarm.SetupStep
import pl.shotclock.budzik.alarm.isSamsung
import pl.shotclock.budzik.alarm.setupStepsFor
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppButtonStyle
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.CardTint
import pl.shotclock.budzik.ui.theme.ControlOffColor
import pl.shotclock.budzik.ui.theme.HighlightColor
import pl.shotclock.budzik.ui.theme.LilacTint
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.OnAccent
import pl.shotclock.budzik.ui.theme.PeachTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.accentSurface
import pl.shotclock.budzik.ui.theme.cardSurface
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.floatingBy
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.ui.theme.rememberPulse

private const val FloatMillis = 2200

/** Karta z wyjaśnieniem, jak działa budzik. */
private class IntroPage(
    @param:DrawableRes val image: Int,
    val tint: CardTint,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
)

private val introPages = listOf(
    IntroPage(R.drawable.ill_toothbrush, MintTint, R.string.onboarding_title, R.string.onboarding_description),
    IntroPage(R.drawable.ill_mug, PeachTint, R.string.onboarding_photo_title, R.string.onboarding_photo_description),
    IntroPage(R.drawable.ill_flame, LilacTint, R.string.onboarding_streak_title, R.string.onboarding_streak_description),
)

// Trzy karty z wyjaśnieniem i na końcu lista zgód.
private val PageCount = introPages.size + 1

/** Onboarding z prawdziwymi zgodami telefonu. Wygląd jest w [OnboardingContent]. */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val setup = rememberSetupState()
    OnboardingContent(
        steps = setup.steps,
        onRequest = setup.onRequest,
        onFinish = onFinish,
        modifier = modifier,
    )
}

/** Wygląd onboardingu: przewijane w bok karty, kropki stron i przycisk „Dalej” / „Zaczynamy”. */
@Composable
fun OnboardingContent(
    steps: List<SetupStepState>,
    onRequest: (SetupStep) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { PageCount }
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == PageCount - 1

    // Systemowe „wstecz” cofa o jedną kartę, a z pierwszej zamyka ekran jak zwykle.
    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    Column(modifier.fillMaxSize().systemBarsPadding()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.minTouchTarget)
                .padding(horizontal = Spacing.small),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (!isLastPage) {
                SkipButton(onClick = { scope.launch { pagerState.animateScrollToPage(PageCount - 1) } })
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            if (page < introPages.size) {
                IntroPageContent(introPages[page])
            } else {
                SetupPage(steps = steps, onRequest = onRequest)
            }
        }
        PageDots(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = Spacing.medium),
        )
        AppButton(
            text = stringResource(if (isLastPage) R.string.onboarding_start else R.string.onboarding_next),
            onClick = {
                if (isLastPage) {
                    onFinish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenEdge)
                .padding(bottom = Spacing.medium),
        )
    }
}

@Composable
private fun IntroPageContent(page: IntroPage) {
    val float = rememberPulse(FloatMillis, label = "onboarding float")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenEdge),
        verticalArrangement = Arrangement.Center,
    ) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .entranceFrom(rememberEntrance(order = 0)),
            tint = page.tint,
            shape = AppShapes.heroCard,
            verticalSpacing = Spacing.medium,
        ) {
            Image(
                painter = painterResource(page.image),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = Spacing.medium)
                    .size(Sizes.illustrationOnboarding)
                    .floatingBy(float),
            )
            Text(text = stringResource(page.title), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(page.description),
                style = MaterialTheme.typography.bodyLarge,
                color = page.tint.secondaryText,
            )
        }
    }
}

/** Ostatnia karta: zgody, o które prosimy, każda z wyjaśnieniem, po co jest. */
@Composable
private fun SetupPage(
    steps: List<SetupStepState>,
    onRequest: (SetupStep) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenEdge),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        Column(
            modifier = Modifier.entranceFrom(rememberEntrance(order = 0)),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Text(text = stringResource(R.string.setup_title), style = MaterialTheme.typography.headlineLarge)
            Text(
                text = stringResource(R.string.setup_description),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )
        }
        steps.forEachIndexed { index, state ->
            SetupStepRow(
                state = state,
                onRequest = { onRequest(state.step) },
                modifier = Modifier.entranceFrom(rememberEntrance(order = index + 1)),
            )
        }
    }
}

@Composable
private fun SetupStepRow(
    state: SetupStepState,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val texts = state.step.texts()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.illustrationHistoryBox)
                .cardSurface(shape = AppShapes.thumbnailSmall),
            contentAlignment = Alignment.Center,
        ) {
            // Opis nie jest potrzebny, bo obok ikony jest nazwa zgody.
            Icon(painterResource(texts.icon), contentDescription = null)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        ) {
            Text(text = stringResource(texts.title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(texts.description),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            if (!state.done) {
                AppButton(
                    text = stringResource(
                        if (state.opensSettings) R.string.permission_action_settings else R.string.permission_action_allow,
                    ),
                    onClick = onRequest,
                    modifier = Modifier.padding(top = Spacing.small),
                    style = AppButtonStyle.Secondary,
                )
            }
        }
        if (state.done) DoneBadge()
    }
}

/** Turkusowe kółko z ptaszkiem: zgoda jest już włączona. Pojawia się z lekkim „wyskoczeniem”. */
@Composable
private fun DoneBadge() {
    val pop = rememberEntrance()
    Box(
        modifier = Modifier
            .size(Sizes.setupDoneBadge)
            .graphicsLayer {
                scaleX = 0.5f + 0.5f * pop.value
                scaleY = 0.5f + 0.5f * pop.value
                alpha = pop.value
            }
            .accentSurface(AppShapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = stringResource(R.string.setup_done),
            tint = OnAccent,
        )
    }
}

/** Przycisk „Pomiń”: sam tekst, ale z polem dotyku 48 dp. */
@Composable
private fun SkipButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(AppShapes.pill)
            .clickable(role = Role.Button, onClick = onClick)
            .sizeIn(minWidth = Sizes.minTouchTarget, minHeight = Sizes.minTouchTarget)
            .padding(horizontal = Spacing.smallMedium),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.onboarding_skip),
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
        )
    }
}

/**
 * Kropki stron. Aktywna kropka rozciąga się w biały pasek. Szerokość liczymy przy rysowaniu,
 * więc animacja nie przebudowuje układu ekranu.
 */
@Composable
private fun PageDots(pagerState: PagerState, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.onboarding_page, pagerState.currentPage + 1, PageCount)
    Row(modifier = modifier.semantics { contentDescription = description }) {
        repeat(PageCount) { index ->
            val selected = animateFloatAsState(
                targetValue = if (index == pagerState.currentPage) 1f else 0f,
                label = "page dot",
            )
            Canvas(Modifier.width(Sizes.pageDot * 3).height(Sizes.pageDot)) {
                val dotWidth = size.height + (size.width - size.height) * selected.value
                drawRoundRect(
                    color = lerp(ControlOffColor, HighlightColor, selected.value),
                    topLeft = Offset((size.width - dotWidth) / 2, 0f),
                    size = Size(dotWidth, size.height),
                    cornerRadius = CornerRadius(size.height / 2),
                )
            }
        }
    }
}

private class StepTexts(
    @param:DrawableRes val icon: Int,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
)

private fun SetupStep.texts(): StepTexts = when (this) {
    SetupStep.NOTIFICATIONS -> StepTexts(
        R.drawable.ic_notifications,
        R.string.setup_notifications_title,
        R.string.setup_notifications_description,
    )
    SetupStep.EXACT_ALARMS -> StepTexts(
        R.drawable.ic_alarm,
        R.string.setup_exact_alarms_title,
        R.string.setup_exact_alarms_description,
    )
    SetupStep.FULL_SCREEN -> StepTexts(
        R.drawable.ic_lock,
        R.string.setup_full_screen_title,
        R.string.setup_full_screen_description,
    )
    SetupStep.CAMERA -> StepTexts(R.drawable.ic_camera, R.string.setup_camera_title, R.string.setup_camera_description)
    SetupStep.BATTERY -> StepTexts(
        R.drawable.ic_battery,
        R.string.setup_battery_title,
        if (isSamsung()) R.string.setup_battery_samsung_description else R.string.setup_battery_description,
    )
}

// Podglądy pokazują przykładowy stan: część zgód gotowa, część jeszcze nie.
private val previewSteps = setupStepsFor(34).mapIndexed { index, step ->
    SetupStepState(step = step, done = index % 2 == 1, opensSettings = step == SetupStep.FULL_SCREEN)
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingIntroPreview() {
    ShotClockTheme {
        AppBackground {
            OnboardingContent(steps = previewSteps, onRequest = {}, onFinish = {})
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun OnboardingSetupPreview() {
    ShotClockTheme {
        AppBackground {
            OnboardingContent(steps = previewSteps, onRequest = {}, onFinish = {}, initialPage = PageCount - 1)
        }
    }
}
