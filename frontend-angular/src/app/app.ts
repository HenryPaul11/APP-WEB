import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Producto } from './models/producto';
import { ProductoService } from './services/producto';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {

  usuario = 'admin';
  clave = 'Admin_2026!';

  productos: Producto[] = [];
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanoPagina = 10;

  producto: Producto = {
    nombre: '',
    descripcion: '',
    precio: 0,
    stock: 0,
    activo: true
  };

  editando = false;
  idEditando: number | null = null;

  constructor(private productoService: ProductoService) {}

  ngOnInit(): void {
    this.listarProductos();
  }

  listarProductos(): void {
    this.productoService.listarPaginado(
      this.usuario, this.clave,
      this.paginaActual, this.tamanoPagina
    ).subscribe({
      next: (data) => {
        this.productos = data.content;
        this.totalPaginas = data.totalPages;
        this.totalElementos = data.totalElements;
      },
      error: (error) => {
        console.error('Error al listar productos', error);
      }
    });
  }

  paginaAnterior(): void {
    if (this.paginaActual > 0) {
      this.paginaActual--;
      this.listarProductos();
    }
  }

  paginaSiguiente(): void {
    if (this.paginaActual < this.totalPaginas - 1) {
      this.paginaActual++;
      this.listarProductos();
    }
  }

  guardarProducto(): void {
    if (this.editando && this.idEditando !== null) {
      this.productoService.actualizar(
        this.idEditando,
        this.producto,
        this.usuario,
        this.clave
      ).subscribe({
        next: () => {
          this.listarProductos();
          this.limpiarFormulario();
        },
        error: (error) => {
          console.error('Error al actualizar producto', error);
        }
      });
    } else {
      this.productoService.crear(
        this.producto,
        this.usuario,
        this.clave
      ).subscribe({
        next: () => {
          this.listarProductos();
          this.limpiarFormulario();
        },
        error: (error) => {
          console.error('Error al crear producto', error);
        }
      });
    }
  }

  editarProducto(producto: Producto): void {
    this.editando = true;
    this.idEditando = producto.id ?? null;

    this.producto = {
      nombre: producto.nombre,
      descripcion: producto.descripcion,
      precio: producto.precio,
      stock: producto.stock,
      activo: producto.activo
    };
  }

  eliminarProducto(id: number | undefined): void {
    if (id === undefined) {
      console.error('No se puede eliminar un producto sin id');
      return;
    }

    this.productoService.eliminar(id, this.usuario, this.clave).subscribe({
      next: () => {
        this.listarProductos();
      },
      error: (error) => {
        console.error('Error al eliminar producto', error);
      }
    });
  }

  limpiarFormulario(): void {
    this.editando = false;
    this.idEditando = null;

    this.producto = {
      nombre: '',
      descripcion: '',
      precio: 0,
      stock: 0,
      activo: true
    };
  }
}
