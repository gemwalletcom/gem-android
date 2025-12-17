# Referral Feature Module

This module implements the referral program feature for Gem Wallet.

## Structure

The module follows the standard Gem Wallet feature architecture with two submodules:

### viewmodels
Contains the business logic and state management:
- `ReferralViewModel.kt` - Main ViewModel handling referral data and actions
- `ReferralUIState` - UI state representation

### presents
Contains the UI/presentation layer:
- `ReferralScreen.kt` - Main composable screen
- `ReferralNavigation.kt` - Navigation setup for the feature

## Usage

### Navigation

To navigate to the referral screen:

```kotlin
navController.navigateToReferral()
```

### Integration

Add the referral destination to your navigation graph:

```kotlin
referral(
    onCancel = { navController.popBackStack() }
)
```

## Features

- Display user's referral code
- Show referral statistics (total referrals, rewards)
- Copy referral code to clipboard
- Share referral code

## Dependencies

- `ui` - Shared UI components
- `ui-models` - Domain models
- `data:repositories` - Data layer access
- Jetpack Compose
- Hilt for dependency injection
