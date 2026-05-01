package com.akmeczo.votersystem.server.requests

import java.util.UUID

data class RefreshTokenRequest(val refreshToken: UUID)