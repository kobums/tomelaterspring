package com.gowoobro.tomelater.enums.answer


enum class Ispublic {
    NONE,
    PRIVATE,  // 비공개
    PUBLIC,  // 공개
;

    companion object {
        fun getDisplayName(value: Ispublic): String {
            return when (value) {
                NONE -> ""
                PRIVATE -> "비공개"
                PUBLIC -> "공개"
            }
        }

        fun fromString(value: String): Ispublic? {
            return when (value) {
                "" -> NONE
                "비공개" -> PRIVATE
                "공개" -> PUBLIC
                else -> null
            }
        }
    }
}

