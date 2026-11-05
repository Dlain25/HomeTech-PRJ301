/* HomeTech presentation interactions. Business forms remain in public.js. */
(() => {
    "use strict";
    document.querySelectorAll("[data-menu-toggle]").forEach(toggle => {
        const panel = document.getElementById(toggle.dataset.menuToggle);
        if (!panel) return;
        const desktop = window.matchMedia("(min-width: 992px)");
        let expanded = false;
        function render() {
            panel.hidden = !desktop.matches && !expanded;
            toggle.setAttribute("aria-expanded", String(desktop.matches || expanded));
        }
        toggle.addEventListener("click", () => { expanded = !expanded; render(); });
        panel.closest("nav").addEventListener("keydown", event => {
            if (event.key === "Escape" && !desktop.matches && expanded) {
                expanded = false; render(); toggle.focus();
            }
        });
        desktop.addEventListener("change", () => {
            expanded = false;
            if (!desktop.matches && panel.contains(document.activeElement)) toggle.focus();
            render();
        });
        render();
    });

    document.querySelectorAll(".ht-banner").forEach(banner => {
        const track = banner.querySelector(".ht-banner-track");
        const slides = Array.from(banner.querySelectorAll(".ht-banner-slide"));
        if (!track || slides.length < 2) return;
        let index = Math.max(0, slides.findIndex(slide => slide.classList.contains("active")));
        let busy = false;
        const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
        track.setAttribute("aria-live", "polite");
        function updateAccess() {
            slides.forEach((slide, i) => {
                slide.setAttribute("role", "group");
                slide.setAttribute("aria-roledescription", "slide");
                slide.setAttribute("aria-label", `${i + 1} / ${slides.length}`);
                slide.setAttribute("aria-hidden", String(i !== index));
                slide.inert = i !== index;
                slide.classList.toggle("active", i === index);
            });
        }
        function move(step) {
            if (busy) return;
            const previous = slides[index];
            index = (index + step + slides.length) % slides.length;
            const next = slides[index];
            // A keyboard user leaving a slide must not retain focus on an inert link.
            if (previous.contains(document.activeElement)) {
                banner.querySelector(`[data-banner-step="${step}"]`)?.focus();
            }
            updateAccess();
            if (reducedMotion.matches) return;
            busy = true;
            previous.classList.add("is-leaving"); next.classList.add("is-entering");
            track.classList.add("is-animating", step > 0 ? "is-forward" : "is-backward");
            let timer;
            function finish() {
                clearTimeout(timer);
                next.removeEventListener("animationend", finish);
                previous.classList.remove("is-leaving"); next.classList.remove("is-entering");
                track.classList.remove("is-animating", "is-forward", "is-backward");
                busy = false;
            }
            next.addEventListener("animationend", finish);
            timer = setTimeout(finish, 700);
        }
        banner.querySelectorAll("[data-banner-step]").forEach(button => {
            button.addEventListener("click", () => move(Number(button.dataset.bannerStep)));
        });
        banner.addEventListener("keydown", event => {
            if (/^(INPUT|TEXTAREA|SELECT)$/.test(event.target.tagName)) return;
            if (event.key === "ArrowLeft" || event.key === "ArrowRight") {
                event.preventDefault(); move(event.key === "ArrowRight" ? 1 : -1);
            }
        });
        let touch;
        track.addEventListener("pointerdown", event => {
            if (event.pointerType === "touch") touch = { x: event.clientX, y: event.clientY };
        });
        track.addEventListener("pointerup", event => {
            if (!touch || event.pointerType !== "touch") return;
            const dx = event.clientX - touch.x, dy = event.clientY - touch.y;
            touch = null;
            if (Math.abs(dx) > 40 && Math.abs(dx) > Math.abs(dy)) move(dx < 0 ? 1 : -1);
        });
        track.addEventListener("pointercancel", () => { touch = null; });
        updateAccess();
    });
})();
