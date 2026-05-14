package com.example.aiddproject.kudos.compose.ui

/**
 * Test tag constants for Compose UI tests of the Viết Kudo composer
 * (T034). Centralised here so every component + every test references
 * the same string and refactors stay safe.
 */
object WriteKudoTestTags {
    const val SCREEN: String = "write_kudo_screen"
    const val HEADER: String = "write_kudo_header"

    const val RECIPIENT_FIELD: String = "write_kudo_recipient_field"
    const val RECIPIENT_OVERLAY: String = "write_kudo_recipient_overlay"
    const val RECIPIENT_SEARCH_INPUT: String = "write_kudo_recipient_search_input"
    const val RECIPIENT_ROW_PREFIX: String = "write_kudo_recipient_row_" // + userId

    const val TITLE_INPUT: String = "write_kudo_title_input"
    const val COMMUNITY_STANDARDS_LINK: String = "write_kudo_community_standards_link"

    const val FORMATTING_TOOLBAR: String = "write_kudo_formatting_toolbar"
    const val BOLD_BUTTON: String = "write_kudo_bold_button"
    const val ITALIC_BUTTON: String = "write_kudo_italic_button"
    const val STRIKE_BUTTON: String = "write_kudo_strike_button"
    const val NUMBERED_LIST_BUTTON: String = "write_kudo_numbered_list_button"
    const val LINK_BUTTON: String = "write_kudo_link_button"
    const val QUOTE_BUTTON: String = "write_kudo_quote_button"

    const val MESSAGE_TEXTAREA: String = "write_kudo_message_textarea"
    const val MESSAGE_CHARACTER_COUNTER: String = "write_kudo_message_counter"
    const val MENTION_OVERLAY: String = "write_kudo_mention_overlay"
    const val LINK_DIALOG: String = "write_kudo_link_dialog"

    const val HASHTAG_SECTION: String = "write_kudo_hashtag_section"
    const val HASHTAG_ADD_BUTTON: String = "write_kudo_hashtag_add_button"
    const val HASHTAG_CHIP_PREFIX: String = "write_kudo_hashtag_chip_" // + tagId
    const val HASHTAG_OVERLAY: String = "write_kudo_hashtag_overlay"
    const val HASHTAG_OVERLAY_ROW_PREFIX: String = "write_kudo_hashtag_overlay_row_" // + tagId

    const val IMAGE_SECTION: String = "write_kudo_image_section"
    const val IMAGE_ADD_BUTTON: String = "write_kudo_image_add_button"
    const val IMAGE_THUMBNAIL_PREFIX: String = "write_kudo_image_thumbnail_" // + clientId
    const val IMAGE_REMOVE_BUTTON_PREFIX: String = "write_kudo_image_remove_" // + clientId

    const val ANONYMOUS_TOGGLE: String = "write_kudo_anonymous_toggle"
    const val ANONYMOUS_NICKNAME_INPUT: String = "write_kudo_anonymous_nickname_input"

    const val BOTTOM_ACTION_BAR: String = "write_kudo_bottom_action_bar"
    const val CANCEL_BUTTON: String = "write_kudo_cancel_button"
    const val SEND_BUTTON: String = "write_kudo_send_button"
    const val SEND_BUTTON_TAP_LAYER: String = "write_kudo_send_button_tap_layer"

    const val CONFIRM_DIALOG: String = "write_kudo_confirm_dialog"
    const val CONFIRM_DIALOG_CONFIRM: String = "write_kudo_confirm_dialog_confirm"
    const val CONFIRM_DIALOG_DISMISS: String = "write_kudo_confirm_dialog_dismiss"
}
