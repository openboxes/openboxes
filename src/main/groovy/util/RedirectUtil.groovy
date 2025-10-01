package util

class RedirectUtil {

    static void redirect(controller, context) {
        def referer = controller.request.getHeader("Referer")
        if (referer) {
            controller.redirect(url: referer)
            return
        }

        if (context?.product) {
            controller.redirect(controller: "inventoryItem", action: "showStockCard", id: context?.product?.id)
        } else if (context?.location) {
            controller.redirect(controller: "location", action: "list")
        } else {
            controller.redirect(uri: "/")
        }
    }

}
