// Snippet to add to the generated android/app/.../MainActivity.kt.
// Starting the service on create (rather than only on background) is
// simplest to get working first; tightening it to only run while
// backgrounded is a follow-up optimization once the basic version works.

// import android.content.Intent
// import android.os.Build

// in MainActivity, e.g. inside onCreate():
// val serviceIntent = Intent(this, KeepAliveService::class.java)
// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//     startForegroundService(serviceIntent)
// } else {
//     startService(serviceIntent)
// }
