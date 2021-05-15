import AppKit

@objc(MenuView)
class MenuView: NSButton {
    private var _title: String = "";
    @objc var menuTitle: String? {
        didSet {
            guard let title = self.menuTitle else {
                return
            }
            self._title = title as String
            self.setup()
        }
    }
    
    private var _actions: [NSMenuItem] = []
    @objc var actions: [NSDictionary]? {
        didSet {
            guard let actions = self.actions else {
                return
            }
            
            actions.forEach { details in
                _actions.append(MenuAction(details: details).createNSMenuItem(#selector(sendButtonAction), target: self))
            }
        }
    }
    
    @objc var onPressAction: RCTDirectEventBlock?
    
    override init(frame frameRect: NSRect) {
        super.init(frame: frameRect)
        self.action = #selector(onMenuOpen(_:))
        self.wantsLayer = true
        self.layer?.masksToBounds = true
        self.title = ""
        self.bezelStyle = .inline
        self.target = self
        setup()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setup() {
        let menu = NSMenu()
        _actions.forEach { item in
            menu.addItem(item)
        }
        
        if let menuTitle = self.menuTitle {
            menu.title = menuTitle
        }
        menu.autoenablesItems = false

        self.menu = menu
    }
    
    @objc func onMenuOpen(_ sender: NSButton) {
        guard let event = NSApplication.shared.currentEvent else {
            return
        }
        NSMenu.popUpContextMenu(sender.menu!, with: event, for: sender)
    }
    
    @objc func sendButtonAction(_ menuItem: NSMenuItem) {
        if let onPress = onPressAction, let identifier = menuItem.identifier {
            onPress(["event": identifier.rawValue])
        }
    }
    
//    override func hitTest(_ point: NSPoint) -> NSView? {
//        for subview in self.subviews {
//            if subview.isKind(of: RCTView.classForCoder()) {
//                return subview
//            }
//        }
//        return nil
//    }
}
