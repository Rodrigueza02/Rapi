
const BASE = "/images";

export const dashboardImages = {
  logo: `${BASE}/branding/logo-deliveryflow.png`,
  scooter: `${BASE}/branding/repartidor-scooter.png`,

  products: {
    hamburguesa: `${BASE}/products/hamburguesa.png`,
    pizza: `${BASE}/products/pizza.png`,
    sushi: `${BASE}/products/sushi.png`,
    papas: `${BASE}/products/papas.png`
  },

  drivers: {
    "REP-01": `${BASE}/drivers/rep-01.png`,
    "REP-02": `${BASE}/drivers/rep-02.png`,
    "REP-03": `${BASE}/drivers/rep-03.png`,
    "REP-04": `${BASE}/drivers/rep-04.png`,
    "REP-05": `${BASE}/drivers/rep-05.png`
  }
};

export const getProductImage = (product) =>
  dashboardImages.products[product] || null;

export const getDriverImage = (driverId) =>
  dashboardImages.drivers[driverId] || null;
