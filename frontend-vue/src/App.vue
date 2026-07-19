<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  listarPaginado,
  crearProducto,
  actualizarProducto,
  eliminarProducto
} from './service/productoService'

interface Producto {
  id?: number
  nombre: string
  descripcion: string
  precio: number
  stock: number
  activo: boolean
}

const productoVacio: Producto = {
  nombre: '', descripcion: '', precio: 0, stock: 0, activo: true
}

const usuario = ref('admin')
const clave = ref('Admin_2026!')
const filtro = ref('')
const productos = ref<Producto[]>([])
const formulario = ref<Producto>({ ...productoVacio })
const editando = ref(false)
const mensaje = ref('')

const paginaActual = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)
const tamanoPagina = 10

onMounted(() => cargarProductos())

async function cargarProductos(pagina: number = paginaActual.value) {
  try {
    const datos = await listarPaginado(usuario.value, clave.value, pagina, tamanoPagina, filtro.value)
    productos.value = datos.content
    totalPaginas.value = datos.totalPages
    totalElementos.value = datos.totalElements
    paginaActual.value = datos.number
    mensaje.value = `Pagina ${datos.number + 1} de ${datos.totalPages} (${datos.totalElementos} registros)`
  } catch (error: any) {
    manejarError(error)
  }
}

function paginaAnterior() {
  if (paginaActual.value > 0) {
    cargarProductos(paginaActual.value - 1)
  }
}

function paginaSiguiente() {
  if (paginaActual.value < totalPaginas.value - 1) {
    cargarProductos(paginaActual.value + 1)
  }
}

async function guardar() {
  const producto = {
    ...formulario.value,
    precio: Number(formulario.value.precio),
    stock: Number(formulario.value.stock)
  }

  try {
    if (editando.value && producto.id) {
      await actualizarProducto(producto.id, producto, usuario.value, clave.value)
      mensaje.value = 'Producto actualizado correctamente.'
    } else {
      await crearProducto(producto, usuario.value, clave.value)
      mensaje.value = 'Producto registrado correctamente.'
    }
    limpiarFormulario()
    await cargarProductos()
  } catch (error: any) {
    manejarError(error)
  }
}

function seleccionar(producto: Producto) {
  formulario.value = { ...producto }
  editando.value = true
}

async function eliminar(producto: Producto) {
  try {
    await eliminarProducto(producto.id!, usuario.value, clave.value)
    mensaje.value = 'Producto desactivado correctamente (eliminacion logica).'
    await cargarProductos()
  } catch (error: any) {
    manejarError(error)
  }
}

function limpiarFormulario() {
  formulario.value = { ...productoVacio }
  editando.value = false
}

function manejarError(error: any) {
  if (error.status === 401) mensaje.value = 'Credenciales incorrectas.'
  else if (error.status === 403) mensaje.value = 'Se requiere rol ADMIN para esta accion.'
  else if (error.status === 409) mensaje.value = 'Ya existe un producto con ese nombre.'
  else mensaje.value = error.message || 'Error inesperado.'
}
</script>

<template>
  <main class="contenedor">
    <h1>CRUD de productos - Vue</h1>

    <section class="tarjeta">
      <h2>Credenciales</h2>
      <label>Usuario</label>
      <input v-model="usuario" />
      <label>Contrasena</label>
      <input type="password" v-model="clave" />
    </section>

    <section class="tarjeta">
      <h2>{{ editando ? 'Editar producto' : 'Registrar producto' }}</h2>
      <form @submit.prevent="guardar">
        <label>Nombre</label>
        <input v-model="formulario.nombre" required />

        <label>Descripcion</label>
        <input v-model="formulario.descripcion" required />

        <label>Precio</label>
        <input type="number" v-model.number="formulario.precio" required />

        <label>Stock</label>
        <input type="number" v-model.number="formulario.stock" required />

        <label>
          <input type="checkbox" v-model="formulario.activo" />
          Activo
        </label>

        <div>
          <button type="submit">{{ editando ? 'Actualizar' : 'Guardar' }}</button>
          <button type="button" @click="limpiarFormulario">Limpiar</button>
        </div>
      </form>
    </section>

    <section class="tarjeta">
      <h2>Listado de productos</h2>
      <input v-model="filtro" placeholder="Buscar por nombre" />
      <button @click="cargarProductos(0)">Buscar</button>

      <p class="mensaje">{{ mensaje }}</p>

      <div class="paginacion">
        <button @click="paginaAnterior" :disabled="paginaActual === 0">Anterior</button>
        <span>Pagina {{ paginaActual + 1 }} de {{ totalPaginas }}</span>
        <button @click="paginaSiguiente" :disabled="paginaActual >= totalPaginas - 1">Siguiente</button>
      </div>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Precio</th>
            <th>Stock</th>
            <th>Activo</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in productos" :key="p.id">
            <td>{{ p.id }}</td>
            <td>{{ p.nombre }}</td>
            <td>{{ p.precio }}</td>
            <td>{{ p.stock }}</td>
            <td>{{ p.activo ? 'Si' : 'No' }}</td>
            <td>
              <button @click="seleccionar(p)">Editar</button>
              <button @click="eliminar(p)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </main>
</template>

<style scoped>
.contenedor {
  max-width: 1100px;
  margin: 30px auto;
  font-family: Arial, sans-serif;
}

.tarjeta {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
}

form {
  display: grid;
  gap: 10px;
  max-width: 500px;
}

input {
  padding: 8px;
  display: block;
  width: 100%;
  box-sizing: border-box;
}

input[type='checkbox'] {
  width: auto;
  display: inline;
}

button {
  padding: 8px 12px;
  cursor: pointer;
  margin-right: 6px;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

th, td {
  border: 1px solid #ddd;
  padding: 8px;
}

th {
  background: #f2f2f2;
}

.mensaje {
  color: #333;
  font-style: italic;
}

.paginacion {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 10px 0;
}
</style>
