# Feature Specification: Viết Kudo (Write Kudo composer)

**Frame ID**: `7fFAb-K35a`
**Frame Name**: `[iOS] Sun*Kudos_Viết Kudo_default`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-13
**Status**: Reviewed (review pass 2026-05-13, second pass 2026-05-13)

Sibling / sub-flow frames covered by this same spec (no separate spec files):
- `PV7jBVZU1N` — `[iOS] Sun*Kudos_Gửi lời chúc Kudos` — duplicate of
  this composer in a "with content" variant. Same composable + same
  behavior; treat as the visual state where the user has already
  entered values (or the variant the Sun*Kudos hub's "Send Kudos
  compose" exit points at). Naming difference only — implementer
  renders both frames through a single `WriteKudoScreen`.
- `0le8xKnFE_` — `[iOS] Sun*Kudos_Lỗi chưa điền hết` — validation
  error state surfaced after tapping the disabled Send button.
- `5MU728Tjck` — `[iOS] Sun*Kudos_Gửi lời chúc Kudos_dropdown tên người nhận`
  — recipient search overlay invoked from B.2.
- `aKWA2klsnt` — `[iOS] Sun*Kudos_Gửi lời chúc Kudos_dropdown hashtag`
  — hashtag picker overlay invoked from E.2.

> The sub-flow names referring to `Gửi lời chúc Kudos` confirm that
> `PV7jBVZU1N` and `7fFAb-K35a` are the same screen — Figma uses the
> two Vietnamese phrasings ("Viết Kudo" and "Gửi lời chúc Kudos")
> interchangeably for the composer.

---

## Overview

**Feature**: Viết Kudo — the rich-text composer that lets an
authenticated Sunner send a public recognition message ("Kudo") to a
named teammate.

**Purpose**: Provide the single canonical entry point for creating a
new Kudo record. The form captures the recipient, an award title, a
free-form recognition message (with @mentions + basic rich-text +
optional image attachments), one-to-five hashtags, and an "anonymous
sender" flag. Submission writes a row to `kudos` and returns the user
to the Sun*Kudos hub where the new Kudo appears at the top of the feed.

**Target users**: Any authenticated Sunner. The recipient picker
excludes the current user (self-recognition is rejected server-side
and surfaced inline as a validation error).

**Business context**: Kudos drive bottom-up recognition during SAA
2025; the volume of Kudos sent / received feeds award eligibility
decisions made by the Heads Council. The composer is therefore the
**primary write-path** for the Sun*Kudos feature — it must be
forgiving of partial input (drafts kept while editing), enforce the
content rules above (so feed cards render predictably), and never
silently lose the user's typed content on transient errors.

**Entry points** (from `SCREENFLOW.md`):
1. Sun*Kudos hub (`fO0Kt19sZZ`) — Send-Kudos pill on the hero.
2. Home (`OuH1BUTYT0`) — Floating Action Button (pencil icon).
3. Search Sunner flow (`3jgwke3E8O`) — tapping a user in search
   results opens the composer with the recipient field pre-filled.

**Exit points**:
- Sun*Kudos hub on submit success (the new Kudo appears at the top
  of the All-Kudos feed; the hub triggers a refresh).
- Previous screen on Cancel (Sun*Kudos hub or Home, depending on
  entry).
- Community Standards page on tapping the "Tiêu chuẩn cộng đồng"
  link (B.5).
- Login screen on 401 during submit (global auth handler clears the
  session — same gate as the rest of the authenticated tab tree).

---

## User Scenarios

### US1: Compose and submit a Kudo to a teammate [P1]

**As an** authenticated Sunner
**I want to** select a teammate, give them a title and a written
message with at least one hashtag, optionally attach images, and
press Send
**So that** I can publicly recognize their contribution and have it
appear immediately in the Sun*Kudos feed

**Why this priority**: The composer is the sole write path for the
Kudos feature. Without it nothing else (feed, highlight, stats) has
new data to render. Every other story on the screen is a refinement
of this one.

**Independent Test**: Open Viết Kudo from the Sun*Kudos hub, fill
all four required fields with valid values, press Send → assert the
hub displays the new Kudo at the top of the All-Kudos list.

**Acceptance Scenarios**:

1. **Given** the user has just opened a blank composer,
   **When** they pick a teammate in the Recipient dropdown (B.2),
   type a Danh hiệu (B.4) of 1–100 chars, type a Message (D) of 1–1000
   non-whitespace chars, and add 1–5 hashtags (E),
   **Then** the Send button (I) transitions from disabled to enabled
   and a tap submits the Kudo, shows a loading state on the button,
   and on success navigates back to the Sun*Kudos hub with a refresh.
2. **Given** the user is composing and Send is currently enabled,
   **When** any required field becomes empty (e.g. last hashtag chip
   removed),
   **Then** the Send button reverts to its visually-disabled state.
   A subsequent tap on the disabled button reveals inline errors per
   US3 (does not submit the form).
3. **Given** the user typed "@" followed by ≥1 character in the
   Message textarea,
   **When** the suggestion overlay shows ≥1 colleague,
   **Then** tapping a colleague inserts a `@Nguyễn Văn A` mention at
   the cursor and closes the overlay; the cursor lands immediately
   after the mention.
4. **Given** the user opened the composer from Search Sunner with a
   pre-filled recipient,
   **When** the screen renders,
   **Then** B.2 displays the chosen teammate's name (no "Tìm kiếm"
   placeholder), Send is still disabled until the other required
   fields are filled, and `formDirty` is `false` until the user
   actually edits a field (the prefill alone does NOT trigger
   unsaved-change confirmation on Cancel).

### US2: Cancel composition with unsaved-content protection [P1]

**As a** user composing a Kudo
**I want to** be warned before discarding my work
**So that** I don't accidentally lose a message I spent time on

**Why this priority**: Pairs with US1 — losing content silently is
the worst-case UX for the primary write path. P1 because the
existing test suite (TC_FUN_042 / TC_FUN_052–054) gates this
behavior.

**Independent Test**: Type into Message, press Cancel (or system
back) → assert a confirmation dialog blocks navigation; confirm →
form data cleared and previous screen restored; dismiss → still on
composer with data intact.

**Acceptance Scenarios**:

1. **Given** the user has not touched any field (the form is clean,
   ignoring a pre-filled recipient from Search Sunner),
   **When** the user taps Cancel (H) or system Back,
   **Then** the composer closes immediately and navigates back to
   the previous screen with no dialog.
2. **Given** the user has touched at least one field (Recipient
   picked, Title typed, Message typed, hashtag added, image added,
   or Anonymous toggled),
   **When** the user taps Cancel (H) or system Back,
   **Then** a confirmation dialog appears: "Bạn có chắc muốn hủy
   không? Dữ liệu đã nhập sẽ bị mất." (copy may be revised at
   localization). Confirming clears the form and navigates back;
   dismissing keeps the user on the composer with data intact.

### US3: Show inline validation errors on Send when fields are invalid [P1]

**As a** user pressing Send with bad / missing data
**I want to** see exactly which field is wrong and why
**So that** I can fix it without guessing

**Why this priority**: Validation errors are the single largest
class of user-facing test cases and the validation-error state has
its own Figma frame (`0le8xKnFE_`). P1 because feed-card integrity
downstream depends on recipient + title + message + hashtags all
being valid; the field-level error mapping is non-negotiable.

**Send-disabled vs. tap-reveals-errors pattern**: The Send button
is visually **disabled** while any required field is missing or
invalid (per TC_GUI_006 + TC_FUN_033), BUT tapping the disabled
Send button **still surfaces all field-level error messages
simultaneously** (matching the `0le8xKnFE_` frame and reconciling
TC_FUN_001/003/006/010). This "tap-to-reveal" interaction is the
sole mechanism that exposes US3's errors for the empty-field cases;
the boundary-violation cases (e.g. recipient = self, title > 100
chars, message > 1000 chars) can also surface from server response
on a real submit. Either path produces the same inline error
treatment under the affected field.

**Independent Test**: Open the composer, leave at least one
required field blank, tap Send → assert (a) the form stays put,
(b) an inline error appears under every empty required field, and
(c) Send remains disabled. Then: pick yourself as recipient, fill
the other fields, tap Send → assert the "You cannot send a kudo
to yourself." error appears under B.2 (server-authoritative path).

**Acceptance Scenarios**:

1. **Given** Send is disabled because the recipient field is empty,
   **When** the user taps the disabled Send button,
   **Then** an inline error "Please select a recipient." appears
   under B.2 (alongside any other missing-field errors). The form
   is not submitted.
2. **Given** the user picks themselves as the recipient and fills
   the other fields,
   **When** they tap Send,
   **Then** the request is submitted, the server rejects it via
   RLS, and an inline error "You cannot send a kudo to yourself."
   appears under B.2. The form keeps its data and Send returns to
   enabled when the recipient is changed.
3. **Given** Send is tapped while Danh hiệu is empty,
   **Then** an inline error "Please enter a title for this
   recognition." appears under B.4.
4. **Given** Danh hiệu contains > 100 chars,
   **Then** an inline error "Title is too long (max 100 characters)."
   appears under B.4. (This case may also be enforced client-side
   by capping input length.)
5. **Given** Send is tapped while Message is empty or contains
   only whitespace,
   **Then** an inline error "Please write your recognition message."
   (empty) or "Message cannot be empty." (whitespace-only) appears
   under D.
6. **Given** Message contains > 1000 chars,
   **Then** an inline error "Character limit reached. Please shorten
   your message." appears under D and the character counter shows
   the over-limit count in an error treatment.
7. **Given** Send is tapped while no hashtags are present,
   **Then** an inline error "Please add at least one hashtag."
   appears under E.
8. **Given** any required field's error is showing,
   **When** the user edits that field to a valid value,
   **Then** the inline error for that field is cleared
   immediately; other field errors remain until their fields are
   also fixed.

### US4: Format the message with rich text and @mentions [P2]

**As a** user composing a longer recognition message
**I want to** apply bold / italic / strikethrough, numbered lists,
quotes, hyperlinks, and @mention teammates inline
**So that** the message reads cleanly when it lands in the feed

**Why this priority**: The toolbar (C) and @mention suggestion list
are clearly drawn in Figma and have explicit test cases, but the
feature is usable without them — most kudos are short plain-text
messages. P2.

**Independent Test**: Select text → tap Bold → assert the run
renders as bold in the preview; type "@ng" → assert the suggestion
overlay shows matching colleagues; select one → assert a mention is
inserted; tap Link → assert URL input dialog opens; submit valid
URL → assert link is wrapped around selection.

**Acceptance Scenarios**:

1. **Given** a non-empty selection in the Message textarea,
   **When** the user taps Bold (C.1), Italic (C.2), or Strikethrough
   (C.3),
   **Then** the selection toggles that formatting and the toolbar
   button reflects the active state for the caret position.
2. **Given** the caret is in a paragraph,
   **When** the user taps Numbered list (C.4) or Quote (C.6),
   **Then** the paragraph(s) toggle list/quote formatting.
3. **Given** the user taps Link (C.5),
   **When** the user enters a URL,
   **Then** an invalid URL is rejected with a format error; a valid
   URL wraps the selected text (or inserts a new link).
4. **Given** the user types "@" followed by ≥1 character,
   **Then** a suggestion overlay shows colleagues whose name matches;
   selecting one inserts a `@FullName` mention at the cursor; if no
   colleague matches, the overlay shows an empty state and the input
   keeps the literal "@xyz" text.

### US5: Attach up to 5 images to a Kudo [P2]

**As a** user
**I want to** add photos to my Kudo
**So that** the recognition can include visual context (event
photos, screenshots, etc.)

**Why this priority**: Optional field per design; useful but not
required for an MVP.

**Independent Test**: Tap "+ Image" → pick a JPG → assert thumbnail
appears with an "x" button; repeat until 5 images then assert the
"+ Image" button is hidden; tap "x" on a thumbnail → assert it is
removed and the add button reappears.

**Acceptance Scenarios**:

1. **Given** fewer than 5 attachments exist,
   **When** the user taps the Add Image button (F.5),
   **Then** the system file picker opens and an accepted file
   (JPG/PNG/WEBP ≤ 10 MB) is appended as a thumbnail in F with an
   "x" remove button.
2. **Given** 5 attachments are present,
   **Then** the Add Image button is hidden (per Figma) and no
   further upload is possible.
3. **Given** the user picks a disallowed type (e.g. PDF, GIF) or a
   file larger than 10 MB,
   **Then** the upload is rejected client-side with a format / size
   error message; no thumbnail is added.

### US6: Send the Kudo anonymously [P2]

**As a** user
**I want to** hide my name from the recipient
**So that** I can give kindly without expectation of credit

**Why this priority**: Toggleable extra; defaults to off, so US1 is
unaffected.

**Independent Test**: Check the Anonymous toggle (G), submit a Kudo
→ assert the Kudo is created with `is_anonymous = true` and the
sender's name is replaced by the "Anonymous" fallback string when
viewed in the feed.

**Acceptance Scenarios**:

1. **Given** the Anonymous toggle (G) is unchecked (default),
   **When** the Kudo is sent,
   **Then** `kudos.is_anonymous = false` is persisted and the
   sender's name shows in the feed.
2. **Given** the toggle is checked,
   **When** the Kudo is sent,
   **Then** `kudos.is_anonymous = true` is persisted and the
   sender's name renders as the localized anonymous label in the
   feed (existing `kudos_anonymous_nickname_fallback` string).

### US7: Open the Community Standards page [P3]

**As a** first-time Kudo author
**I want to** read the rules for what counts as a valid award title
**So that** my recognition isn't flagged later

**Why this priority**: Educational link; out-of-flow.

**Independent Test**: Tap "Tiêu chuẩn cộng đồng" (B.5) → assert
navigation to the standards page.

**Acceptance Scenarios**:

1. **Given** the user is on the composer,
   **When** they tap B.5,
   **Then** the Community Standards page opens (in-app screen or
   webview). On dismiss the user returns to the composer with their
   draft intact.

### Edge Cases

- **No recipient list available** — if the user directory fails to
  load (network / RLS failure) and no cached list is available, the
  Recipient dropdown (B.2) is non-interactive (visually disabled —
  the picker overlay does NOT open on tap) and a localized error
  message is rendered inline beneath B.2 with a Retry affordance.
  Cancel still works; Send stays disabled. If a stale cached list
  exists, it is used and a background refresh is attempted.
- **No hashtag suggestions** — the hashtag picker (E.2) may show an
  empty state if the curated tag list is empty; the user can
  dismiss without selecting and the hashtag list stays unchanged.
- **@mention with no matches** — overlay shows empty state, no
  insertion happens, the typed "@xyz" stays in the textarea as
  plain text.
- **Image upload failure (network / 5xx / storage RLS)** — the
  picked file is rejected with a localized error message and no
  thumbnail is added. The form retains all other state. The user
  may retry by re-tapping F.5 with the same or a different file.
  Upload errors do NOT block form submission of the other fields.
- **Submission while offline / 5xx** — the form retains all data,
  `isSending` clears, and a snackbar / inline error surfaces the
  failure. Retry leaves the form intact.
- **401 during submission** — the global auth handler clears the
  session and redirects to Login; the draft is lost (no offline
  draft persistence in this slice).
- **Concurrent submissions** — while `isSending` is true, every
  input (B.2, B.4, C.1–C.6, D, E.2, F.5, image thumbnails, G) AND
  the Send button (I) are non-interactive. The Cancel button (H)
  remains enabled per TC_VIETKUDO_FUN_060 — tapping it during
  submission cancels the in-flight request (request is cancelled
  via `viewModelScope`; the form returns to the editable state with
  data preserved, no navigation).
- **Exactly-boundary inputs** — Title=1 char, Title=100 chars,
  Message=1 non-whitespace char, Message=1000 chars, Hashtags=1,
  Hashtags=5, Images=0, Images=5 are all accepted.
- **System back gesture** — mirrors Cancel button behavior: dirty
  form → confirmation dialog, clean form → immediate back. While
  `isSending = true`, system back behaves like H tap (cancel
  request, stay on screen).

---

## Component Behavior

> Node IDs map to Figma `7fFAb-K35a`. Visual / layout details are
> resolved by the implementation phase via `query_section`.

> **`isSending` disabled-inputs contract** (applies to every
> interactive component below except H): while `isSending = true`,
> the component MUST be non-interactive (tap is a no-op, keyboard
> input is blocked). Once `isSending` returns to false (on success,
> failure, or cancel), each component resumes its normal state.
> H (Cancel) remains interactive throughout — see its entry for
> in-flight semantics.

### A — Header Text (`6885:9292`, kind: label)

- Static instructional text "Gửi lời cám ơn và ghi nhận đến đồng đội".
- No interaction.

### B.1 — Người nhận Label (`6885:9294`, kind: label)

- Static "Người nhận *" with required marker. No interaction.

### B.2 — Recipient Search Dropdown (`6885:9297`, kind: dropdown, REQUIRED)

- **Interaction**: Tap opens the recipient picker overlay (sub-flow
  `5MU728Tjck`) with a search field. Typing filters by name + dept.
  Tap a row → the dropdown collapses and B.2 displays the chosen
  name; the data layer holds the user's `recipient_id`.
- **Pre-fill**: when the screen is entered from Search Sunner with
  a `recipientUserId` argument, B.2 starts populated and `formDirty`
  remains `false` until another field is touched.
- **Validation**:
  - Required — empty on Send → "Please select a recipient."
  - Cannot equal the current authenticated user's id — "You cannot
    send a kudo to yourself." (client should also hide / disable
    the current user in the picker list).
- **State**: Disabled when the user-directory cannot load.
- **Maps to**: `kudos.recipient_id`.

### B.3 — Danh hiệu Label (`6885:9299`, kind: label)

- Static "Danh hiệu *". No interaction.

### B.4 — Danh hiệu Input (`6885:9302`, kind: text input, REQUIRED)

- **Interaction**: Tap activates the input; keyboard appears.
- **Validation**: `string`, 1–100 chars, required.
- **Errors**: empty → "Please enter a title for this recognition.";
  > 100 → "Title is too long (max 100 characters)."
- **Maps to**: `kudos.title`.

### B.5 — Community Standards Link (`6885:9303`, kind: text link)

- **Interaction**: Tap navigates to the Community Standards page (a
  separate screen or in-app webview). On return, draft is intact.
- No validation.

### C — Formatting Toolbar (`6885:9306`, container)

- Parent container for C.1–C.6. No direct interaction.

### C.1 — Bold (`6885:9307`, kind: toggle button)

- Tap toggles bold formatting on the current selection / next typed
  run in D. The button reflects the active formatting at the caret.

### C.2 — Italic (`6885:9309`, kind: toggle button)

- Same pattern as C.1 for italic.

### C.3 — Strikethrough (`6885:9311`, kind: toggle button)

- Same pattern as C.1 for strikethrough.

### C.4 — Numbered List (`6885:9313`, kind: toggle button)

- Tap toggles numbered-list mode on the paragraph(s) containing the
  caret / selection.

### C.5 — Link (`6885:9315`, kind: button)

- Tap opens a URL input dialog. On confirm, validates URL format —
  invalid → inline error; valid → wraps the selected text in a
  hyperlink (or inserts the URL if no selection).

### C.6 — Quote (`6885:9317`, kind: toggle button)

- Toggles blockquote formatting on the paragraph at the caret.

### D — Message Textarea (`6885:9322`, kind: rich text input, REQUIRED)

- **Interaction**: Tap activates the textarea; keyboard appears.
  Supports rich-text formatting via C.*, @mentions (see below), and
  a live character counter rendered alongside D.
- **@mention**: typing "@" followed by ≥1 char triggers a
  suggestion overlay listing colleagues whose name matches; tap a
  row → insert `@FullName` mention at the cursor + close overlay;
  no matches → empty state; typed text is preserved literally.
- **Character counter**: displays `current/1000`; starts at "0/1000",
  updates live on every keystroke; over-1000 puts the counter into
  an error treatment (precise visual is implementation-time concern).
- **Validation**: `string`, 1–1000 non-whitespace chars after trim,
  required.
- **Errors**: empty → "Please write your recognition message.";
  whitespace-only → "Message cannot be empty."; > 1000 →
  "Character limit reached. Please shorten your message."
- **State**: standard `isSending` disabled contract (see top of
  Component Behavior section).
- **Maps to**: `kudos.message` (rich-text-encoded payload; encoding
  format finalized at implementation time).

> **Hint label (D.1)**: static "Bạn có thể \"@ + tên\" để nhắc tới
> đồng nghiệp khác" rendered below D. No interaction.

### E — Hashtag Section (`6885:9324`, container, REQUIRED)

- Holds E.1 label "Hashtag *" and E.2 tag group.

#### E.2 — Tag Group (`6885:9328`, kind: tag input)

- **Interaction**: Tap "+ Hashtag" opens the hashtag picker overlay
  (sub-flow `aKWA2klsnt`). Picking a tag adds a chip with an "x"
  remove button. Tapping "x" removes that tag. Closing the picker
  without selecting leaves the list unchanged.
- **State**: "+ Hashtag" button is disabled once 5 chips exist.
- **Validation**: `array<string>`, 1–5 entries, required.
- **Errors**: empty on Send → "Please add at least one hashtag.";
  > 5 → "Maximum 5 hashtags allowed." (also enforced by disabling
  the add button).
- **Maps to**: `kudos.tags` (JSON array of tag strings).

### F — Image Section (`6885:9346`, container, OPTIONAL)

- Holds F.1 label "Image", F.2 / F.3 / F.4 / F.2b thumbnails (up to
  5), and F.5 Add Image button.

#### F.1 — Image Label (`6885:9347`, kind: label)

- Static "Image". No required marker (this section is optional).
  No interaction.

#### F.5 — Add Image Button (`6885:9355`, kind: button)

- **Interaction**: Tap opens the system file picker. On accept,
  appends a thumbnail (F.2 … F.2b). The button hides when 5
  thumbnails exist.
- **Validation**: file type must be one of `jpg / png / webp`
  (OWASP A04 — reject `gif`, `pdf`, etc.); file size ≤ 10 MB
  client-side. Rejected files surface an inline error; no thumbnail
  is added.

#### F.2 / F.3 / F.4 / F.2b — Image Thumbnail (Node IDs `6885:9352`, `6885:9353`, `6885:9354`, `6885:9356` — kind: thumbnail with delete)

- **Interaction**: Tap the "x" on a thumbnail to remove that image.
  No validation.
- **Maps to**: `kudos.image_ids` (array of asset references).

> Node ID `6885:9355` is the Add Image button (F.5), not a
> thumbnail — listed separately above.

### G — Anonymous Toggle (`6885:9363`, kind: checkbox, OPTIONAL, default false)

- **Interaction**: Tap toggles `is_anonymous`. Unchecked by default.
- **Maps to**: `kudos.is_anonymous` (boolean).

### H — Cancel Button (`6891:16834`, kind: button)

- **Interaction (normal)**: Tap → if `formDirty = false`, navigate
  back immediately; if `formDirty = true`, show the unsaved-changes
  confirmation dialog (US2 Scenario 2).
- **Interaction (during submit — `isSending = true`)**: Stays
  enabled per TC_VIETKUDO_FUN_060. Tap cancels the in-flight POST
  via `viewModelScope` job cancellation; `isSending` clears; the
  form returns to its editable state with all data preserved; no
  navigation occurs. The user may edit further or re-tap Send.
  (Pressing Cancel a second time, with the form now idle, follows
  the normal interaction rules above.)

### I — Send Button (`6891:16833`, kind: primary action)

- **Interaction**: Tap.
- **Derived visual enabled state**: `recipientId != null &&
  title.isNotBlank() && message.trim().isNotEmpty() &&
  hashtags.isNotEmpty() && !isSending`. Disabled (greyed) visually
  whenever any clause is false (TC_GUI_006, TC_FUN_033).
- **On tap — disabled branch (any required field missing/invalid)**:
  1. Do NOT submit.
  2. Reveal inline error messages under every empty/invalid
     required field at once (this is the `0le8xKnFE_` frame
     contract — covers TC_FUN_001/003/006/010).
  3. Send remains disabled; the user must fix each field, which
     clears its own error on edit (US3 Scenario 8).
- **On tap — enabled branch (all client validations pass)**:
  1. Submit the form to `/rest/v1/kudos` (POST).
  2. Set `isSending = true`; loading indicator replaces button
     label; all inputs disable per the section-top contract; H
     remains enabled per its own entry.
  3. **Success** → navigate back to the Sun*Kudos hub and trigger a
     refresh of Highlight + All-Kudos.
  4. **Field error from server (4xx with field map — e.g. self-send
     rejection)** → surface inline errors next to each affected
     field; the form keeps its data; Send returns to enabled when
     fix conditions are met (US3 Scenario 2).
  5. **Generic error (5xx / network)** → snackbar or inline error;
     form data preserved; `isSending` clears; inputs re-enable.
  6. **401** → handed to the global auth handler → redirect to
     Login (draft is lost).
  7. **User cancelled mid-submit** → see H entry; treated as a
     non-error: `isSending` clears, no toast, no navigation.

### Bottom action bar (composite)

- H and I render in a sticky bar that stays visible during scroll
  (TC_VIETKUDO_GUI_008). The bar is part of the screen scaffold,
  not the scrollable form column.

---

## Data Requirements

### Input fields (`kudos` row)

| Field          | Type                | Required | Constraints                                                          | UI source         |
| -------------- | ------------------- | -------- | -------------------------------------------------------------------- | ----------------- |
| `recipient_id` | uuid (FK → users)   | Yes      | Must not equal `auth.uid()`                                          | B.2               |
| `title`        | string              | Yes      | 1–100 chars                                                          | B.4               |
| `message`      | rich-text string    | Yes      | 1–1000 non-whitespace chars after trim; may contain `@mentions`, bold/italic/strikethrough, numbered lists, links, quotes | D                 |
| `tags`         | array<string>       | Yes      | 1–5 entries; values from curated hashtag list                        | E.2               |
| `image_ids`    | array<asset ref>    | No       | 0–5 entries; per-file constraints: jpg/png/webp, ≤ 10 MB             | F                 |
| `is_anonymous` | boolean             | No       | Default `false`                                                      | G                 |

### Derived / server-set fields (not shown in UI)

- `sender_id = auth.uid()` (anti-spoofing — never read from client
  payload).
- `created_at` = server time.
- Initial reaction / view counts = zero.

### Pre-fill arguments (when entering from Search Sunner)

- `recipientUserId?: string` — opens the form with B.2 populated.
  `formDirty` starts `false`.

### Reference data loaded by the screen

- **Colleagues directory** — required for B.2 dropdown and D
  @mention suggestion overlay. Should exclude `auth.uid()`. Source
  shared with Search Sunner / hub spotlight; a stale-but-cached list
  is acceptable on cold open (refresh in background).
- **Hashtag catalog** — required for E.2 picker. Curated server-side.

### Local state held by the composer

- `recipientId: String?`, `recipientName: String?`
- `title: String`
- `message: RichTextValue` (encoding TBD at plan time)
- `tags: List<String>`
- `images: List<UploadedImage>` (each with optimistic local id +
  remote asset ref once uploaded)
- `isAnonymous: Boolean`
- `formDirty: Boolean` — flipped to true on the first user edit
  AFTER initial mount (prefill alone does NOT set it).
- `isSending: Boolean`
- `fieldErrors: Map<FieldKey, ErrorMessageRes>` — cleared per field
  on edit.
- `mentionQuery: String?` — non-null while @ overlay is open.
- `linkDialog: LinkDialogState?` — non-null while C.5 dialog is open.

---

## API Requirements *(predicted)*

| Endpoint                                  | Method | Purpose                                                                 | Triggered by                              |
| ----------------------------------------- | ------ | ----------------------------------------------------------------------- | ----------------------------------------- |
| `/rest/v1/users` (Supabase REST + RLS)    | GET    | List sunners excluding self for recipient picker + @mention overlay      | Screen mount; recipient dropdown open; "@…" typing |
| `/rest/v1/hashtag_catalog`                | GET    | Curated hashtag list                                                    | "+ Hashtag" tap (overlay open)            |
| `/storage/v1/object/{bucket}/{path}`      | POST   | Upload an image attachment; returns asset ref to push into `image_ids`   | Add Image (F.5) on file picked            |
| `/storage/v1/object/{bucket}/{path}`      | DELETE | Delete an attached image when removed before submit                     | Thumbnail "x" tap (cleanup of unused asset) |
| `/rest/v1/kudos`                          | POST   | Insert new Kudo row (`{recipient_id, title, message, tags, image_ids, is_anonymous}`). Server enforces RLS + self-send rejection | Send (I) on tap                           |

> All endpoints assume Supabase REST + RLS per Constitution II / IV.
> The client never sends `sender_id` — the server derives it from
> the auth context. RLS policy on `kudos` rejects `recipient_id =
> auth.uid()` with a structured error mapped to the inline
> "You cannot send a kudo to yourself." message.

---

## State Management

### Local component state

- Holds the entire composer model above. Owned by a `WriteKudoViewModel`
  (per Constitution II) and exposed as a single `StateFlow<WriteKudoUiState>`
  to the composable.

### Global state needs

- Auth (current user id) — read for self-send filter + sender header
  injection by the Supabase client.
- Snackbar host (cross-feature) — used for generic submit errors
  and image-upload errors.
- Sun*Kudos hub refresh trigger — on successful submit the hub
  refetches Highlight + All-Kudos (the navigation result handles
  this — composer does not directly call hub repositories).

### Cache / invalidation

- **Colleagues directory** — fetched lazily on first picker open;
  cached for the session.
- **Hashtag catalog** — fetched lazily on first picker open; cached
  for the session.
- **Uploaded images that are abandoned** — when the user removes a
  thumbnail before submit, the client deletes the storage object so
  no orphaned assets accumulate.
- **On submit success** — the hub's All-Kudos list invalidates so
  the new Kudo appears at the top. The composer state is discarded
  on screen disposal.

### Optimistic updates

- None inside the composer screen itself. The hub may optimistically
  prepend the new Kudo card on `Send → success` but that lives in
  the hub's spec, not here.

### Concurrency

- Send is single-flight — while `isSending = true` further taps on
  I are ignored and all inputs are disabled. The Cancel control
  remains available; cancelling cancels the in-flight request.

---

## Navigation Rules

| From                                | To                              | Trigger                                              |
| ----------------------------------- | ------------------------------- | ---------------------------------------------------- |
| Sun*Kudos hub `fO0Kt19sZZ`          | Viết Kudo (this screen)         | Tap Send-Kudos pill on hub hero                       |
| Home `OuH1BUTYT0`                   | Viết Kudo (this screen)         | Tap compose FAB (pencil icon)                        |
| Search Sunner `3jgwke3E8O`          | Viết Kudo (this screen)         | Tap a user in results — prefill `recipientUserId`    |
| Viết Kudo                            | Sun*Kudos hub                   | Send success                                          |
| Viết Kudo                            | Previous screen                 | Cancel / system back (with confirm if dirty)         |
| Viết Kudo                            | Community Standards page        | Tap B.5                                              |
| Viết Kudo                            | Login screen                    | Global auth handler on 401 during submit              |

---

## Accessibility Requirements *(behavior only)*

- Every interactive element (B.2, B.4, C.1–C.6, D, E.2, F.5, image
  thumbnails, G, H, I, B.5) MUST expose a `contentDescription` /
  semantic label localized in both VN and EN.
- The Recipient dropdown announces its selection ("Người nhận,
  Nguyễn Văn A, dropdown").
- Required fields announce their required state.
- Validation errors are announced (live region) when they appear so
  TalkBack users hear which field is invalid.
- Toolbar toggle buttons announce active/inactive state.
- The character counter announces "X of 1000" politely on stop-typing,
  and assertively if the limit is exceeded.
- Sticky bottom action bar is reachable via TalkBack swipe order
  after the form body.
- Minimum touch target 48×48dp on every interactive element
  (Constitution III).

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The composer MUST persist a new `kudos` row containing
  the five required fields above plus the optional anonymous + image
  fields when the user submits a valid form.
- **FR-002**: The Send button MUST be visually disabled until all
  required fields hold valid values AND the form is not currently
  submitting. Tapping the disabled button MUST surface inline
  errors under every missing / invalid required field
  simultaneously (the `0le8xKnFE_` reveal-all-errors behavior).
- **FR-003**: Users MUST be able to compose with rich-text
  formatting (bold, italic, strikethrough, numbered list, quote,
  hyperlink) and inline `@mentions`.
- **FR-004**: The system MUST validate field constraints client-side
  for instant feedback AND re-validate server-side (RLS + checks) on
  submit — the client MUST NOT be the source of truth for "no
  self-send".
- **FR-005**: The system MUST present field-specific inline errors
  on Send when validation fails, in the user's locale, mapped to the
  field component below the label.
- **FR-006**: On successful submit the system MUST navigate the user
  back to the Sun*Kudos hub and refresh the All-Kudos feed so the
  new Kudo appears at the top.
- **FR-007**: On Cancel / system back, the system MUST require a
  confirmation dialog ONLY when the form is dirty (any field touched
  after mount; a pre-filled recipient does NOT count as dirty).
- **FR-008**: Image attachments MUST be limited to ≤ 5 per Kudo, of
  type jpg / png / webp, and ≤ 10 MB each (client-side validation
  rejects others with a localized error).
- **FR-009**: Pressing Send while a submission is already in flight
  MUST be a no-op; the form MUST disable all inputs while submitting.
- **FR-010**: Removing an uploaded image attachment before submit
  MUST delete the storage object so abandoned assets do not
  accumulate.
- **FR-011**: A failed image upload (network / 5xx / storage RLS
  error) MUST surface a localized error, leave the form state
  otherwise unchanged, and allow retry without blocking submission
  of the other fields.
- **FR-012**: Tapping Cancel while a submission is in flight MUST
  cancel the in-flight request and return the form to its
  editable state with data preserved (no navigation).

### Technical Requirements

- **TR-001**: All state changes flow through `WriteKudoViewModel`
  exposed as `StateFlow<WriteKudoUiState>` — no business logic
  inside composables (Constitution II).
- **TR-002**: Self-send rejection is enforced by Postgres RLS on
  `kudos`; the client may pre-filter the recipient picker but MUST
  NOT be the authoritative gate (Constitution IV).
- **TR-003**: Image uploads use Supabase Storage with bucket-level
  RLS scoped to the authenticated user (Constitution II + IV).
- **TR-004**: The composer MUST respect system back / predictive
  back gestures (Android `BackHandler` wired when `formDirty`).
- **TR-005**: Strings are externalized to VN + EN resources; no
  hard-coded literals in the UI layer (Constitution III + parity
  with Home / Sun*Kudos hub).

### Key Entities

- **Kudo** — the row this screen produces. Fields: `id`,
  `sender_id`, `recipient_id`, `title`, `message`, `tags`,
  `image_ids`, `is_anonymous`, `created_at`.
- **User (Sunner)** — referenced for sender + recipient + @mention.
- **Hashtag** — curated catalog used by E.2.

---

## Constitution Alignment

| Principle | Compliance approach                                                                                                                                                          |
| --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| I.        | Feature-first layout under `kudos/compose/` with `ui/`, `data/`, `domain/`. Composables < 150 LOC; no logic in composables.                                                  |
| II.       | Kotlin/Compose/Supabase idioms; `StateFlow` UI state; `viewModelScope` for submit + uploads; Compose toolbar + textarea use Material 3 components.                            |
| III.      | Material 3 inputs, dialogs, snackbar; 48dp touch targets; TalkBack labels; dynamic color; light + dark themes; system font scaling.                                          |
| IV.       | No secrets in client; RLS on `kudos` + Storage; client-side validation backed by server-side enforcement; @mentions never trigger arbitrary lookups outside the directory RLS scope. |
| V.        | TDD — failing unit tests for validators (title / message / hashtags / self-send), failing UI test for Send-disabled→enabled transition, failing repository test for submit RLS error mapping, written before implementation. |

---

## Out of Scope

- **Draft persistence across sessions** — if the app is killed mid-
  compose, the draft is lost.
- **Editing or deleting an already-published Kudo** — covered by a
  separate frame (`419VXmMy6I` "Màn Sửa bài viết — edit mode") if
  prioritized later.
- **Scheduling a Kudo to send later** — not in this slice.
- **Reactions / hearts on draft preview** — happens on the feed,
  not in the composer.
- **Multiple recipients per Kudo** — single recipient only.
- **Localization beyond VN + EN** — same locales as Home / hub.
- **Visual / pixel specs** — handled by `momorph.implement-ui` via
  `query_section` at implementation time.

---

## Dependencies

- [x] Constitution document (`.momorph/constitution.md`).
- [ ] API specifications (`.momorph/contexts/api-docs.yaml`) — to be
      added or updated with the `kudos` insert + storage upload
      endpoints during plan phase.
- [ ] Database schema (`.momorph/contexts/database-schema.sql`) —
      verify `kudos` table + RLS policies cover `is_anonymous`,
      `image_ids`, self-send rejection.
- [x] Screen flow (`.momorph/SCREENFLOW.md`) — entries for this
      screen + its three sub-flow states added.
- [x] Sun*Kudos hub spec (`fO0Kt19sZZ`) — entry point + post-submit
      refresh contract.
- [ ] Search Sunner spec (`3jgwke3E8O`) — prefill contract.

---

## Notes

- The validation-error frame `0le8xKnFE_` is the same composer with
  all inline errors visible at once; it is NOT a separate screen
  and shares this spec.
- Recipient + hashtag dropdowns (`5MU728Tjck`, `aKWA2klsnt`) are
  sub-flow overlays rendered inside this screen; they reuse the
  shared dropdown chrome from the hub filters (`uUvW6Qm1ve` pattern
  validated by the hub implementation).
- `kudos.message` rich-text encoding is deliberately left to plan
  phase — candidates: Markdown subset, ProseMirror-style JSON,
  AndroidX rich-text. The constraint is round-trippability with the
  hub's `KudosFeedCard` renderer.
- Image asset references in `kudos.image_ids` should be Supabase
  storage paths (not absolute URLs), so signed URLs can be issued
  per-view by the hub.
