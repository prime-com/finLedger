import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  ApiErrorResponse,
  FinancialSummary,
  TransactionFilter,
  TransactionRequest,
  TransactionResponse
} from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getTransactions(filter?: TransactionFilter): Observable<TransactionResponse[]> {
    let params = new HttpParams();

    if (filter) {
      if (filter.type) {
        params = params.set('type', filter.type);
      }
      if (filter.category && filter.category.trim()) {
        params = params.set('category', filter.category.trim());
      }
      if (filter.startDate) {
        params = params.set('startDate', filter.startDate);
      }
      if (filter.endDate) {
        params = params.set('endDate', filter.endDate);
      }
    }

    return this.http.get<TransactionResponse[]>(this.apiUrl, { params }).pipe(
      catchError(this.handleError)
    );
  }

  getTransactionById(id: number): Observable<TransactionResponse> {
    return this.http.get<TransactionResponse>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  createTransaction(request: TransactionRequest): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(this.apiUrl, request).pipe(
      catchError(this.handleError)
    );
  }

  updateTransaction(id: number, request: TransactionRequest): Observable<TransactionResponse> {
    return this.http.put<TransactionResponse>(`${this.apiUrl}/${id}`, request).pipe(
      catchError(this.handleError)
    );
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getFinancialSummary(startDate?: string, endDate?: string): Observable<FinancialSummary> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }

    return this.http.get<FinancialSummary>(`${this.apiUrl}/summary`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected network error occurred. Please check your connection.';
    let apiError: ApiErrorResponse | null = null;

    if (error.error && typeof error.error === 'object') {
      apiError = error.error as ApiErrorResponse;
      if (apiError.message) {
        errorMessage = apiError.message;
      }
      if (apiError.validationErrors && Object.keys(apiError.validationErrors).length > 0) {
        const details = Object.entries(apiError.validationErrors)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join(', ');
        errorMessage = `${errorMessage} (${details})`;
      }
    } else if (error.status === 0) {
      errorMessage = 'Cannot connect to FinLedger backend. Ensure Spring Boot is running on port 8080.';
    } else if (error.status === 404) {
      errorMessage = 'The requested transaction was not found.';
    }

    return throwError(() => ({
      status: error.status,
      message: errorMessage,
      apiError
    }));
  }
}
