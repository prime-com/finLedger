import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TransactionService } from './transaction.service';
import { FinancialSummary, TransactionRequest, TransactionResponse } from '../models/transaction.model';
import { environment } from '../../../environments/environment';

describe('TransactionService', () => {
  let service: TransactionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TransactionService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(TransactionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch transactions list without filters', () => {
    const mockTransactions: TransactionResponse[] = [
      {
        id: 1,
        title: 'Salary',
        amount: 50000,
        category: 'Salary',
        type: 'INCOME',
        transactionDate: '2026-03-01',
        notes: null,
        createdAt: '2026-03-01T10:00:00'
      }
    ];

    service.getTransactions().subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].title).toBe('Salary');
    });

    const req = httpMock.expectOne(environment.apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockTransactions);
  });

  it('should apply filters to query parameters', () => {
    service.getTransactions({ type: 'EXPENSE', category: 'Food' }).subscribe();

    const req = httpMock.expectOne(req =>
      req.url === environment.apiUrl &&
      req.params.get('type') === 'EXPENSE' &&
      req.params.get('category') === 'Food'
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should create a transaction via POST', () => {
    const newTx: TransactionRequest = {
      title: 'Coffee',
      amount: 150,
      category: 'Food',
      type: 'EXPENSE',
      transactionDate: '2026-03-02'
    };

    const mockResponse: TransactionResponse = {
      id: 2,
      ...newTx,
      notes: null,
      createdAt: '2026-03-02T10:00:00'
    };

    service.createTransaction(newTx).subscribe(res => {
      expect(res.id).toBe(2);
      expect(res.title).toBe('Coffee');
    });

    const req = httpMock.expectOne(environment.apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newTx);
    req.flush(mockResponse);
  });

  it('should fetch financial summary via GET /summary', () => {
    const mockSummary: FinancialSummary = {
      totalIncome: 75000,
      totalExpense: 25000,
      netBalance: 50000,
      totalTransactions: 5,
      incomeCount: 2,
      expenseCount: 3
    };

    service.getFinancialSummary().subscribe(res => {
      expect(res.netBalance).toBe(50000);
      expect(res.totalIncome).toBe(75000);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/summary`);
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);
  });

  it('should delete a transaction via DELETE /{id}', () => {
    service.deleteTransaction(10).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
