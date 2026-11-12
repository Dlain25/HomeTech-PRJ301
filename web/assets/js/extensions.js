'use strict';
document.querySelectorAll('[data-delivery-date]').forEach(button => {
  button.addEventListener('click', () => {
    const input = document.getElementById('desiredDeliveryDate');
    if (input) input.value = button.dataset.deliveryDate;
  });
});
