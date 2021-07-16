class RCTUIMenuItem: UIMenuItem {
    var id: String?

    init(title: String, action: Selector, id: String?) {
        self.id = id
        super.init(title: title, action: action)
    }
}
