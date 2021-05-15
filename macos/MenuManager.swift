@objc(RCTUIMenu)
class RCTUIMenuManager: RCTViewManager {
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    override func view() -> NSView? {
        return MenuView(frame: NSRect())
    }
}

