import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import EasyMDE from 'easymde';
import { ConfigService } from '../../services/config.service';
import { Agent } from '../../models/agent.model';
import { MarkdownPipe } from '../../pipes/markdown.pipe';

@Component({
  selector: 'app-agent-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MarkdownPipe],
  templateUrl: './agent-management.component.html',
  styleUrl: './agent-management.component.css'
})
export class AgentManagementComponent implements OnInit {
  agents: Agent[] = [];
  readonly agentPageSize = 5;
  currentAgentPage = 1;

  selectedAgent: Agent | null = null;
  agentForm: FormGroup;
  isAgentFormVisible = false;
  
  @ViewChild('mdeTextarea') mdeTextarea!: ElementRef<HTMLTextAreaElement>;
  private easyMDE: EasyMDE | null = null;

  constructor(
    private fb: FormBuilder,
    private configService: ConfigService
  ) {
    this.agentForm = this.fb.group({
      id: [null],
      title: ['', Validators.required],
      content: ['', Validators.required]
    });
  }

  get totalAgentPages(): number {
    return Math.max(1, Math.ceil(this.agents.length / this.agentPageSize));
  }

  get paginatedAgents(): Agent[] {
    const startIndex = (this.currentAgentPage - 1) * this.agentPageSize;
    return this.agents.slice(startIndex, startIndex + this.agentPageSize);
  }

  get agentRangeStart(): number {
    if (this.agents.length === 0) {
      return 0;
    }
    return (this.currentAgentPage - 1) * this.agentPageSize + 1;
  }

  get agentRangeEnd(): number {
    if (this.agents.length === 0) {
      return 0;
    }
    return Math.min(this.currentAgentPage * this.agentPageSize, this.agents.length);
  }

  ngOnInit() {
    this.loadAgents();
  }

  loadAgents() {
    this.configService.getAllAgents().subscribe(agents => {
      this.agents = [...agents].sort((a, b) => (a.title ?? '').localeCompare(b.title ?? ''));
      this.clampAgentPage();
    });
  }

  previousAgentPage() {
    if (this.currentAgentPage > 1) {
      this.currentAgentPage--;
    }
  }

  nextAgentPage() {
    if (this.currentAgentPage < this.totalAgentPages) {
      this.currentAgentPage++;
    }
  }

  openAgentForm(agent: Agent | null = null) {
    this.selectedAgent = agent;
    if (agent) {
      this.agentForm.patchValue(agent);
    } else {
      this.agentForm.reset({
        id: null,
        title: '',
        content: ''
      });
    }
    this.isAgentFormVisible = true;

    // Initialize or update EasyMDE
    setTimeout(() => {
      if (this.mdeTextarea && !this.easyMDE) {
        this.easyMDE = new EasyMDE({ 
          element: this.mdeTextarea.nativeElement,
          spellChecker: false,
          maxHeight: '400px'
        });
        
        if (agent) {
          this.easyMDE.value(agent.content || '');
        }

        this.easyMDE.codemirror.on('change', () => {
          this.agentForm.patchValue({ content: this.easyMDE?.value() || '' });
        });
      } else if (this.easyMDE) {
        this.easyMDE.value(agent ? agent.content || '' : '');
      }
    });
  }

  closeAgentForm() {
    if (this.easyMDE) {
      this.easyMDE.toTextArea();
      this.easyMDE = null;
    }
    this.isAgentFormVisible = false;
    this.selectedAgent = null;
  }

  saveAgent() {
    if (this.agentForm.valid) {
      const agent = this.agentForm.value as Agent;
      if (this.selectedAgent?.id) {
        this.configService.updateAgent(this.selectedAgent.id, agent).subscribe(() => {
          this.loadAgents();
          this.closeAgentForm();
        });
      } else {
        this.configService.createAgent(agent).subscribe(() => {
          this.currentAgentPage = this.totalAgentPages + 1;
          this.loadAgents();
          this.closeAgentForm();
        });
      }
    }
  }

  deleteAgent(id: number | undefined, event?: Event) {
    event?.stopPropagation();

    if (!id) {
      return;
    }

    if (confirm('Are you sure you want to delete this agent?')) {
      this.configService.deleteAgent(id).subscribe(() => {
        this.loadAgents();
      });
    }
  }

  private clampAgentPage() {
    this.currentAgentPage = Math.min(Math.max(1, this.currentAgentPage), this.totalAgentPages);
  }
}
