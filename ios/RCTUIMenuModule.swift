@objc(RCTUIMenuModule)
class RCTUIMenuModule: NSObject {
    var resolve: RCTPromiseResolveBlock?
    var reject: RCTPromiseRejectBlock?
    @objc(showMenu:withHeaderConfig:withResolver:withRejecter:)
    func showMenu(actions: [NSDictionary], withHeaderConfig: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        guard #available(iOS 13.0, *) else {
            return resolve(nil)
        }
        
        DispatchQueue.main.async {
            guard let viewController = RCTPresentedViewController() else {
                return resolve(nil)
            }
            
            print(123, viewController.canBecomeFirstResponder)
            self.resolve = resolve
            self.reject = reject

            var items: [UIMenuItem] = []
            actions.forEach { menuAction in
                let id = menuAction["id"] as? NSString
                let title = menuAction["title"] as? NSString
                items.append(RCTUIMenuItem(title: title as String? ?? "", action: #selector(self.onItemSelected(sender:)), id: id as String?))
            }
            print(456, items)
            viewController.becomeFirstResponder()
            UIMenuController.shared.menuItems = items
            UIMenuController.shared.arrowDirection = .default
            UIMenuController.shared.showMenu(from: viewController.view, rect: CGRect(x: 100, y: 100, width: 200, height: 200))
            print(789)
        }
    }
    
    @objc func onItemSelected(sender: RCTUIMenuItem) {
        if let resolve = self.resolve {
            resolve(sender.id)
        }
        self.resolve = nil
        self.reject = nil
    }
}
