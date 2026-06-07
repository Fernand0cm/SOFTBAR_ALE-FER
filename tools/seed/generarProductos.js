/**
 * Generador puro del catalogo de prueba (sin dependencias de Firebase).
 * Separado de seed.js para poder verificarlo en seco.
 */

// Categorias: IVA y si llevan control de stock (lo contable: botellas y latas).
const CATEGORIAS = [
  { nombre: "Cafes", iva: 0.10, stock: false, objetivo: 25, precio: [1.1, 2.4],
    base: ["Cafe solo", "Cafe cortado", "Cafe con leche", "Cafe doble", "Cafe bombon",
      "Capuchino", "Cafe americano", "Cafe descafeinado", "Cafe con hielo", "Latte",
      "Cafe vienes", "Carajillo", "Cafe irlandes", "Manchado", "Cafe largo"] },
  { nombre: "Infusiones", iva: 0.10, stock: false, objetivo: 15, precio: [1.2, 2.2],
    base: ["Te verde", "Te negro", "Te rojo", "Manzanilla", "Poleo menta", "Tila",
      "Rooibos", "Te chai", "Infusion frutas", "Te con leche"] },
  { nombre: "Refrescos", iva: 0.10, stock: true, objetivo: 30, precio: [1.5, 3.0],
    base: ["Cola", "Cola zero", "Naranja", "Limon", "Tonica", "Gaseosa", "Bitter",
      "Te frio", "Aguarica", "Granizado limon", "Mosto", "Nestea", "Aquarius",
      "Red Bull", "Agua mineral", "Agua con gas"] },
  { nombre: "Cervezas", iva: 0.21, stock: true, objetivo: 30, precio: [1.6, 3.5],
    base: ["Cana", "Doble", "Tercio", "Jarra", "Clara", "Cerveza sin", "Tostada",
      "Cerveza negra", "IPA", "Radler", "Mahou", "Estrella", "Alhambra", "Voll-Damm"] },
  { nombre: "Vinos", iva: 0.21, stock: true, objetivo: 25, precio: [1.8, 4.5],
    base: ["Tinto crianza", "Tinto reserva", "Tinto joven", "Rioja", "Ribera",
      "Blanco verdejo", "Blanco albarino", "Rosado", "Vino dulce", "Tinto de verano",
      "Cava", "Vermut rojo", "Vermut blanco", "Fino"] },
  { nombre: "Licores", iva: 0.21, stock: true, objetivo: 25, precio: [3.0, 7.0],
    base: ["Gin tonic", "Ron cola", "Whisky", "Vodka naranja", "Orujo", "Pacharan",
      "Baileys", "Licor cafe", "Brandy", "Mojito", "Aperol spritz", "Tequila"] },
  { nombre: "Desayunos", iva: 0.10, stock: false, objetivo: 15, precio: [1.5, 4.5],
    base: ["Tostada con tomate", "Tostada con mantequilla", "Croissant", "Napolitana",
      "Palmera", "Churros", "Porras", "Magdalena", "Bizcocho", "Tostada iberico"] },
  { nombre: "Tapas", iva: 0.10, stock: false, objetivo: 30, precio: [1.5, 4.0],
    base: ["Tortilla espanola", "Croquetas", "Patatas bravas", "Patatas alioli",
      "Ensaladilla rusa", "Pulpo a la gallega", "Boquerones", "Aceitunas", "Almendras",
      "Jamon iberico", "Queso manchego", "Chorizo", "Morcilla", "Gambas al ajillo",
      "Champinones", "Pimientos padron", "Calamares", "Albondigas"] },
  { nombre: "Raciones", iva: 0.10, stock: false, objetivo: 25, precio: [5.0, 12.0],
    base: ["Racion de jamon", "Tabla de quesos", "Tabla iberica", "Calamares a la romana",
      "Chopitos", "Mejillones", "Zamburinas", "Secreto iberico", "Solomillo al whisky",
      "Pollo al ajillo", "Oreja a la plancha", "Bravas grandes"] },
  { nombre: "Bocadillos", iva: 0.10, stock: false, objetivo: 25, precio: [2.5, 5.5],
    base: ["Bocadillo de jamon", "Bocadillo de tortilla", "Bocadillo de calamares",
      "Bocadillo de lomo", "Bocadillo de queso", "Bocadillo de chorizo", "Pepito de ternera",
      "Bocadillo vegetal", "Bocadillo de bacon", "Bocadillo de atun"] },
  { nombre: "Montaditos", iva: 0.10, stock: false, objetivo: 20, precio: [1.2, 2.8],
    base: ["Montadito de lomo", "Montadito de jamon", "Montadito de tortilla",
      "Montadito de queso", "Montadito de chorizo", "Montadito de salmon",
      "Montadito de anchoa", "Montadito vegetal", "Montadito de pringa"] },
  { nombre: "Postres", iva: 0.10, stock: false, objetivo: 20, precio: [2.5, 5.0],
    base: ["Flan casero", "Tarta de queso", "Natillas", "Arroz con leche", "Helado",
      "Tiramisu", "Brownie", "Fruta del tiempo", "Yogur natural", "Cuajada"] },
];

const SUFIJOS = ["", " (especial)", " de la casa", " grande", " mediano", " pequeno",
  " premium", " artesano", " del dia"];

function precioAleatorio([min, max]) {
  const v = min + Math.random() * (max - min);
  return Math.round(v * 20) / 20; // a multiplos de 0.05
}

function generarProductos() {
  const productos = [];
  let indice = 1;
  for (const cat of CATEGORIAS) {
    const vistos = new Set();
    let i = 0;
    while (vistos.size < cat.objetivo) {
      const baseNombre = cat.base[i % cat.base.length];
      const vuelta = Math.floor(i / cat.base.length);
      const nombre = (baseNombre + SUFIJOS[vuelta % SUFIJOS.length]).trim();
      i++;
      if (vistos.has(nombre)) continue;
      vistos.add(nombre);

      const codigo = "SB" + String(indice).padStart(5, "0");
      indice++;
      productos.push({
        codigoBarras: codigo,
        nombre,
        precio: precioAleatorio(cat.precio),
        tipoIva: cat.iva,
        activo: true,
        categoria: cat.nombre,
        controlarStock: cat.stock,
        stock: cat.stock ? 12 + Math.floor(Math.random() * 37) : 0,
        stockMinimo: cat.stock ? 6 : 0,
      });
    }
  }
  return productos;
}

module.exports = { CATEGORIAS, generarProductos };
