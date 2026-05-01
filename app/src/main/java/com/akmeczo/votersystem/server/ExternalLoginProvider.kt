package com.akmeczo.votersystem.server

enum class ExternalLoginProvider(val serverName: String) {
    Google("Google"),
    Facebook("Facebook"),
    Neptun("Saml")
}