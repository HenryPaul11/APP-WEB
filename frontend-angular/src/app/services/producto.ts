import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Producto } from '../models/producto';

export interface PageResponse {
  content: Producto[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductoService {

  private apiUrl = 'http://localhost:8081/api/productos';

  constructor(private http: HttpClient) {}

  private crearHeaders(usuario: string, clave: string): HttpHeaders {
    const token = btoa(`${usuario}:${clave}`);

    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Basic ${token}`
    });
  }

  listar(usuario: string, clave: string, nombre?: string): Observable<Producto[]> {
    const headers = this.crearHeaders(usuario, clave);

    if (nombre && nombre.trim() !== '') {
      return this.http.get<Producto[]>(
        `${this.apiUrl}?nombre=${nombre}`,
        { headers }
      );
    }

    return this.http.get<Producto[]>(this.apiUrl, { headers });
  }

  listarPaginado(
    usuario: string,
    clave: string,
    page: number,
    size: number,
    nombre?: string
  ): Observable<PageResponse> {
    const headers = this.crearHeaders(usuario, clave);
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (nombre && nombre.trim() !== '') {
      params = params.set('nombre', nombre.trim());
    }

    return this.http.get<PageResponse>(
      `${this.apiUrl}/paginado`,
      { headers, params }
    );
  }

  crear(producto: Producto, usuario: string, clave: string): Observable<Producto> {
    const headers = this.crearHeaders(usuario, clave);

    return this.http.post<Producto>(
      this.apiUrl,
      producto,
      { headers }
    );
  }

  actualizar(
    id: number,
    producto: Producto,
    usuario: string,
    clave: string
  ): Observable<Producto> {
    const headers = this.crearHeaders(usuario, clave);

    return this.http.put<Producto>(
      `${this.apiUrl}/${id}`,
      producto,
      { headers }
    );
  }

  eliminar(id: number, usuario: string, clave: string): Observable<void> {
    const headers = this.crearHeaders(usuario, clave);

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`,
      { headers }
    );
  }
}
