package com.example.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

/**
 * Standardized navigation transition animation specs for the Compose Navigation Graph.
 * Provides butter-smooth slide and fade physics across screens.
 */
object NavigationTransitions {

  private const val ENTER_DURATION = 320
  private const val EXIT_DURATION = 280

  /**
   * Smooth horizontal slide into view from the right with subtle fade.
   */
  fun enterFromRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
      towards = AnimatedContentTransitionScope.SlideDirection.Left,
      animationSpec = tween(ENTER_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(ENTER_DURATION, easing = EaseInOutCubic))
  }

  /**
   * Smooth horizontal exit to the left with subtle fade.
   */
  fun exitToLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
      towards = AnimatedContentTransitionScope.SlideDirection.Left,
      animationSpec = tween(EXIT_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(EXIT_DURATION, easing = EaseInOutCubic))
  }

  /**
   * Smooth pop enter slide into view from the left with subtle fade.
   */
  fun popEnterFromLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
      towards = AnimatedContentTransitionScope.SlideDirection.Right,
      animationSpec = tween(ENTER_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(ENTER_DURATION, easing = EaseInOutCubic))
  }

  /**
   * Smooth pop exit slide out of view to the right with subtle fade.
   */
  fun popExitToRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
      towards = AnimatedContentTransitionScope.SlideDirection.Right,
      animationSpec = tween(EXIT_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(EXIT_DURATION, easing = EaseInOutCubic))
  }
}
