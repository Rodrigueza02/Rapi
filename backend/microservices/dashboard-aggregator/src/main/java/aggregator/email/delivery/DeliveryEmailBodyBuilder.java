package aggregator.email.delivery;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Component
public class DeliveryEmailBodyBuilder {

    public String buildHtml(String nombre, String orderId,
                            List<String> items, BigDecimal total) {
        String itemsHtml = items.isEmpty() ? "<li>Productos no especificados</li>"
                : items.stream().map(i -> "<li>" + i + "</li>")
                       .reduce("", String::concat);
        String totalFmt = NumberFormat.getNumberInstance(new Locale("es", "CO"))
                .format(total);
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#fff">
              <div style="background:#FF7900;padding:24px;text-align:center">
                <h1 style="color:#fff;margin:0">¡Tu pedido llegó con éxito!</h1>
              </div>
              <div style="padding:24px">
                <p>Hola, <strong>%s</strong>:</p>
                <p>Tu pedido <strong>#%s</strong> ha sido <strong>ENTREGADO</strong>.</p>
                <h3>Resumen:</h3>
                <ul style="line-height:1.8">%s</ul>
                <div style="background:#FFF7F0;border-left:4px solid #FF7900;padding:12px;margin:16px 0">
                  <strong>Total: $%s COP</strong>
                </div>
                <p style="color:#666">¡Gracias por confiar en <strong>DeliveryFlow</strong>!</p>
              </div>
            </div>""".formatted(nombre, orderId, itemsHtml, totalFmt);
    }

    public String buildText(String nombre, String orderId,
                            List<String> items, BigDecimal total) {
        String itemsList = items.isEmpty() ? "- Productos no especificados"
                : items.stream().map(i -> "- " + i).reduce("", (a, b) -> a + "\n" + b);
        return "Hola, %s:\n\n¡Tu pedido llegó con éxito!\nPedido #%s — ENTREGADO\n\nResumen:\n%s\n\nTotal: $%s COP\n\n¡Gracias por confiar en DeliveryFlow!"
                .formatted(nombre, orderId, itemsList,
                        NumberFormat.getNumberInstance(new Locale("es", "CO")).format(total));
    }
}
