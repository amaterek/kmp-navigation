import SwiftUI
import sample

@main
struct iOSSample: App {

	init() {
		InitHelperKt.doInit()
	}

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}