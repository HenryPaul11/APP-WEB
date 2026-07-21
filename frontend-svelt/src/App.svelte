<script>
  import { onMount } from 'svelte'
  import {
    listarPaginado,
    crearProducto,
    actualizarProducto,
    eliminarProducto
  } from './service/productoService'

  const productoVacio = {
    nombre: '', descripcion: '', precio: 0, stock: 0, activo: true
  }

  let usuario = 'admin'
  let clave = 'Admin_2026!'
  let filtro = ''
  let productos = []
  let formulario = { ...productoVacio }
  let editando = false
  let mensaje = ''

  let paginaActual = 0
  let totalPaginas = 0
  let totalElementos = 0
  const tamanoPagina = 10

  onMount(() => cargarProductos())

  async function cargarProductos(pagina = paginaActual) {
    try {
      const datos = await listarPaginado(usuario, clave, pagina, tamanoPagina, filtro)
      productos = datos.content
      totalPaginas = datos.totalPages
      totalElementos = datos.totalElements
      paginaActual = datos.number
      mensaje = `Pagina ${datos.number + 1} de ${datos.totalPages} (${datos.totalElementos} registros)`
    } catch (error) {
      manejarError(error)
    }
  }

  function paginaAnterior() {
    if (paginaActual > 0) {
      cargarProductos(paginaActual - 1)
    }
  }

  function paginaSiguiente() {
    if (paginaActual < totalPaginas - 1) {
      cargarProductos(paginaActual + 1)
    }
  }

  async function guardar() {
    const producto = {
      ...formulario,
      precio: Number(formulario.precio),
      stock: Number(formulario.stock)
    }

    try {
      if (editando && producto.id) {
        await actualizarProducto(producto.id, producto, usuario, clave)
        mensaje = 'Producto actualizado correctamente.'
      } else {
        await crearProducto(producto, usuario, clave)
        mensaje = 'Producto registrado correctamente.'
      }
      limpiarFormulario()
      await cargarProductos()
    } catch (error) {
      manejarError(error)
    }
  }

  function seleccionar(producto) {
    formulario = { ...producto }
    editando = true
  }

  async function eliminar(producto) {
    try {
      await eliminarProducto(producto.id, usuario, clave)
      mensaje = 'Producto desactivado correctamente (eliminacion logica).'
      await cargarProductos()
    } catch (error) {
      manejarError(error)
    }
  }

  function limpiarFormulario() {
    formulario = { ...productoVacio }
    editando = false
  }

  function manejarError(error) {
    if (error.status === 401) mensaje = 'Credenciales incorrectas.'
    else if (error.status === 403) mensaje = 'Se requiere rol ADMIN para esta accion.'
    else if (error.status === 409) mensaje = 'Ya existe un producto con ese nombre.'
    else mensaje = error.message || 'Error inesperado.'
  }
</script>

<main class="contenedor">
  <h1>CRUD de productos - Svelte</h1>

  <section class="tarjeta">
    <h2>{editando ? 'Editar producto' : 'Registrar producto'}</h2>
    <form on:submit|preventDefault={guardar}>
      <label>Nombre</label>
      <input bind:value={formulario.nombre} required />

      <label>Descripcion</label>
      <input bind:value={formulario.descripcion} required />

      <label>Precio</label>
      <input type="number" bind:value={formulario.precio} required />

      <label>Stock</label>
      <input type="number" bind:value={formulario.stock} required />

      <label>
        <input type="checkbox" bind:checked={formulario.activo} />
        Activo
      </label>

      <div>
        <button type="submit">{editando ? 'Actualizar' : 'Guardar'}</button>
        <button type="button" on:click={limpiarFormulario}>Limpiar</button>
      </div>
    </form>
  </section>

  <section class="tarjeta">
    <h2>Listado de productos</h2>
    <input bind:value={filtro} placeholder="Buscar por nombre" />
    <button on:click={() => cargarProductos(0)}>Buscar</button>

    <p class="mensaje">{mensaje}</p>

    <div class="paginacion">
      <button on:click={paginaAnterior} disabled={paginaActual === 0}>Anterior</button>
      <span>Pagina {paginaActual + 1} de {totalPaginas}</span>
      <button on:click={paginaSiguiente} disabled={paginaActual >= totalPaginas - 1}>Siguiente</button>
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
        {#each productos as p (p.id)}
          <tr>
            <td>{p.id}</td>
            <td>{p.nombre}</td>
            <td>{p.precio}</td>
            <td>{p.stock}</td>
            <td>{p.activo ? 'Si' : 'No'}</td>
            <td>
              <button on:click={() => seleccionar(p)}>Editar</button>
              <button on:click={() => eliminar(p)}>Eliminar</button>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  </section>
</main>

<style>
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
