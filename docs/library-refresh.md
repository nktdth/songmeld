# Library refresh and cover-memory follow-up

## Behavior

- Observe external MediaStore audio changes while permission is granted and the
  activity is started. Debounce notification bursts for 350 ms before rescanning;
  unregister and cancel scans when stopped, disposed or permission is revoked.
- Rescan on return to the foreground after the first successful load, so changes
  made in other music/file apps become visible.
- Preserve the last successful song list during refresh and on refresh errors.
  Show a progress bar while refreshing and a retry notice after a failed refresh.
  The initial load/error still uses the full-page states.
- Associate asynchronous results with their search query and sort choice. A new
  query must not display old-query rows during the background computation.
- Returning from Settings restores the library without replaying the wordmark's
  entrance animation. Correctly consume Scaffold insets before adding IME padding.

## Media queries and artwork

A null MediaStore cursor is a read failure rather than a successful empty library.
Long cursor traversals check coroutine cancellation. Android can still complete a
native blocking query before cancellation is observed.

Artwork decoding uses a power-of-two sample based on the longest image edge,
bounding the decoded edge to approximately 512 pixels even for panoramas.
A semaphore limits extraction/decoding to three simultaneous requests. Cache is
checked again after acquiring a slot, and cancelled requests do not populate it.
Blocking native codecs are not interrupted mid-call; the slot remains occupied
until that call returns. Compressed embedded picture bytes are still supplied by
Android's metadata API before downsampling.

## Checks

Extended `tools/check_library_query.sh` compiles the actual state and sampling
helpers along with the query implementation. Checks cover retained list identity
through refresh/failure, successful empty results, extreme panorama dimensions,
power-of-two samples, and the existing matching/sorting/cancellation cases.

Device checks still needed: copy/delete/update music externally, revoke/regrant
permission, quickly rescan/change query, return from Settings, inspect keyboard
spacing, and scroll files with large or malformed embedded pictures. Confirm the
last successful list remains usable while scanning and the retry notice is clear.
