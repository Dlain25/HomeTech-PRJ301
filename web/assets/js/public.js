(() => {
    "use strict";
    const fallback = document.body.dataset.placeholder;
    function recoverImage(img) {
        if (!fallback || img.dataset.fallbackApplied) return;
        img.dataset.fallbackApplied = "true";
        img.src = fallback;
    }
    document.addEventListener("error", event => {
        if (event.target.matches?.("img[data-product-image]")) recoverImage(event.target);
    }, true);
    document.querySelectorAll("img[data-product-image]").forEach(img => {
        if (img.complete && img.naturalWidth === 0) recoverImage(img);
    });
    document.querySelectorAll("[data-gallery-src]").forEach(button => {
        button.addEventListener("click", () => {
            const main = document.getElementById("main-product-image");
            delete main.dataset.fallbackApplied;
            main.src = button.dataset.gallerySrc;
            document.querySelectorAll("[data-gallery-src]").forEach(other => {
                other.classList.toggle("is-selected", other === button);
                other.setAttribute("aria-pressed", String(other === button));
            });
        });
    });
    // Keep the displayed VND text separate from the integer sent to the servlet.
    // BigInt avoids precision loss across the full DECIMAL(18,2) integer range.
    const priceLimit = 9999999999999999n;
    const priceStep = 100000n;
    document.querySelectorAll("[data-vnd-price]").forEach(input => {
        const raw = document.createElement("input");
        raw.type = "hidden";
        raw.name = input.name;
        input.removeAttribute("name");
        input.removeAttribute("pattern");
        input.after(raw);
        const buttons = [...document.querySelectorAll(`[data-price-target="${input.id}"]`)];
        function parsePrice() {
            const text = input.value.trim();
            if (text === "") return { value: null };
            if (!/^(?:[0-9]{1,16}|[1-9][0-9]{0,2}(?:\.[0-9]{3})+)(?:[đ₫])?$/.test(text)) return { invalid: true };
            const value = BigInt(text.replace(/[.đ₫]/g, ""));
            return value <= priceLimit ? { value } : { invalid: true };
        }
        function sync(format = false) {
            const parsed = parsePrice();
            input.setCustomValidity(parsed.invalid ? "Vui lòng nhập số nguyên không âm, tối đa 9.999.999.999.999.999đ." : "");
            input.setAttribute("aria-invalid", String(Boolean(parsed.invalid)));
            raw.value = parsed.invalid || parsed.value === null ? "" : parsed.value.toString();
            if (format && !parsed.invalid) {
                input.value = parsed.value === null ? "" : raw.value.replace(/\B(?=(\d{3})+(?!\d))/g, ".") + "đ";
            }
            buttons.forEach(button => {
                const down = button.dataset.priceStep === "-1";
                button.disabled = !parsed.invalid && parsed.value !== null
                        && (down ? parsed.value === 0n : parsed.value === priceLimit);
            });
            return parsed;
        }
        function step(direction) {
            const parsed = sync();
            if (parsed.invalid) { input.reportValidity(); return; }
            const next = (parsed.value ?? 0n) + BigInt(direction) * priceStep;
            input.value = (next < 0n ? 0n : next > priceLimit ? priceLimit : next).toString();
            sync(true);
        }
        input.addEventListener("input", () => sync());
        input.addEventListener("blur", () => sync(true));
        input.addEventListener("keydown", event => {
            if (event.key === "ArrowUp" || event.key === "ArrowDown") {
                event.preventDefault();
                step(event.key === "ArrowUp" ? 1 : -1);
            }
        });
        buttons.forEach(button => button.addEventListener("click", () => step(button.dataset.priceStep)));
        input.form.addEventListener("submit", event => {
            if (sync(true).invalid) { event.preventDefault(); input.reportValidity(); }
        });
        sync(true);
    });
    document.getElementById("sort")?.addEventListener("change", () => {
        document.getElementById("catalog-filter").requestSubmit();
    });
    const form = document.getElementById("variant-form");
    if (!form) return;
    const select = document.getElementById("variant-select");
    const quantity = document.getElementById("quantity");
    const buy = document.getElementById("add-to-cart");
    function refresh() {
        const option = select.selectedOptions[0];
        const stock = Number(option.dataset.stock);
        // BigDecimal is formatted by JSP; do not lose precision through JavaScript Number.
        document.getElementById("variant-price").textContent = option.dataset.priceLabel;
        document.getElementById("variant-sku").textContent = option.dataset.sku;
        const label = document.getElementById("variant-stock");
        label.textContent = stock > 0 ? `Còn hàng · ${stock} sản phẩm` : "Hết hàng";
        label.className = "stock-label " + (stock > 0 ? "in-stock" : "out-stock");
        quantity.max = String(stock);
        quantity.disabled = stock <= 0;
        if (stock > 0 && (!Number.isInteger(Number(quantity.value)) || Number(quantity.value) < 1 || Number(quantity.value) > stock)) quantity.value = "1";
        buy.disabled = stock <= 0 || form.dataset.cartReady !== "true";
    }
    select.addEventListener("change", refresh);
    form.addEventListener("submit", event => {
        const stock = Number(select.selectedOptions[0].dataset.stock);
        const amount = Number(quantity.value);
        if (form.dataset.cartReady !== "true" || stock <= 0 || !Number.isInteger(amount) || amount < 1 || amount > stock) {
            event.preventDefault();
            quantity.reportValidity();
        }
    });
    refresh();
})();

