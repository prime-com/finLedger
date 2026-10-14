import { Component, Input } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FinancialSummary } from '../../core/models/transaction.model';

@Component({
  selector: 'app-summary-cards',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './summary-cards.component.html',
  styleUrls: ['./summary-cards.component.css']
})
export class SummaryCardsComponent {
  @Input() summary: FinancialSummary | null = null;
  @Input() loading: boolean = false;
}
