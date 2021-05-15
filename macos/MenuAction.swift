class MenuAction {
    var identifer: NSUserInterfaceItemIdentifier?
    var image: NSImage?
    var title: String
    var isEnabled: Bool = true
    var isHidden: Bool = false
    var state: NSControl.StateValue = .off
    var subactions: [MenuAction] = []

    init(details: NSDictionary) {
        if let identifier = details["id"] as? String {
            self.identifer = NSUserInterfaceItemIdentifier(identifier)
        }
        
        if #available(macOS 11.0, *) {
            if let imageName = details["image"] as? String {
                if let image = NSImage(systemSymbolName: imageName, accessibilityDescription: nil) {
                    if let imageColor = details["imageColor"] {
                        self.image = image.imageWithTintColor(tintColor: RCTConvert.nsColor(imageColor))
                    } else {
                        self.image = image
                    }
                }
            }
        }
        
        if let title = details["title"] as? String {
            self.title = title as String;
        } else {
            self.title = "";
        }
        
        if let attributes = details["attributes"] as? NSDictionary {
            if (attributes["disabled"] as? Bool) == true {
                self.isEnabled = false
            } else {
                self.isEnabled = true
            }
            if (attributes["hidden"] as? Bool) == true {
                self.isHidden = true
            }
        }

        if let state = details["state"] as? NSString {
            if state == "on" {
                self.state = .on
            }
            if state == "off" {
                self.state = .off
            }
            if state == "mixed" {
                self.state = .mixed
            }
        }
        
        if let subactions = details["subactions"] as? NSArray {
            if subactions.count > 0 {
                self.subactions.removeAll()
                for subaction in subactions {
                    self.subactions.append(MenuAction(details: subaction as! NSDictionary))
                }
            }
        }
    }
    
    func createNSMenuItem(_ selector: Selector?, target: AnyObject?) -> NSMenuItem {
        let item = NSMenuItem(title: self.title, action: selector, keyEquivalent: "")
        
        item.target = target
        item.identifier = self.identifer
        item.image = self.image
        item.isEnabled = self.isEnabled
        item.isHidden = self.isHidden
        item.state = state
        
        if subactions.count > 0 {
            var submenuActions: [NSMenuItem] = []
            subactions.forEach { subaction in
                submenuActions.append(subaction.createNSMenuItem(selector, target: target))
            }
            let submenu = NSMenu()
            submenuActions.forEach { subitem in
                submenu.addItem(subitem)
            }
            item.submenu = submenu
            item.action = nil
            item.target = nil
        }
        
        return item
    }
}

extension NSImage {
    func imageWithTintColor(tintColor: NSColor) -> NSImage {
        if !self.isTemplate {
            return self
        }
        
        let image = self.copy() as! NSImage
        image.lockFocus()
        tintColor.set()
        let imageRect = NSRect(origin: .zero, size: image.size)
        imageRect.fill(using: .sourceAtop)
        image.unlockFocus()
        image.isTemplate = false
        return image
    }
}

