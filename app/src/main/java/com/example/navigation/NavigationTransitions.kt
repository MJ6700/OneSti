package com.example.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

/**
 * Standardized navigation transition animation specs for the Compose Navigation Graph.
 * Provides butter-smooth slide and fade physics across screens based on Material 3 motion specs.
 */
object NavigationTransitions {

  // Material 3 Emphasized motion curves for fluid, non-jarring screen choreography
  private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
  private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

  private const val ENTER_DURATION = 300
  private const val EXIT_DURATION = 250

  /**
   * Smooth horizontal slide into view from the right with subtle fade.
   */
  fun enterFromRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
      towards = AnimatedContentTransitionScope.SlideDirection.Start,
      animationSpec = tween(ENTER_DURATION, easing = EmphasizedDecelerate)
    ) + fadeIn(animationSpec = tween(ENTER_DURATION, easing = EmphasizedDecelerate))
  }

  /**
   * Smooth horizontal exit to the left with subtle fade.
   */
  fun exitToLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
      towards = AnimatedContentTransitionScope.SlideDirection.Start,
      animationSpec = tween(EXIT_DURATION, easing = EmphasizedAccelerate)
    ) + fadeOut(animationSpec = tween(EXIT_DURATION, easing = EmphasizedAccelerate))
  }

  /**
   * Smooth pop enter slide into view from the left with subtle fade.
   */
  fun popEnterFromLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
      towards = AnimatedContentTransitionScope.SlideDirection.End,
      animationSpec = tween(ENTER_DURATION, easing = EmphasizedDecelerate)
    ) + fadeIn(animationSpec = tween(ENTER_DURATION, easing = EmphasizedDecelerate))
  }

  /**
   * Smooth pop exit slide out of view to the right with subtle fade.
   */
  fun popExitToRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
      towards = AnimatedContentTransitionScope.SlideDirection.End,
      animationSpec = tween(EXIT_DURATION, easing = EmphasizedAccelerate)
    ) + fadeOut(animationSpec = tween(EXIT_DURATION, easing = EmphasizedAccelerate))
  }
}
